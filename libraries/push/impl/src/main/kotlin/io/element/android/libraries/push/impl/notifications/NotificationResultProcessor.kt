/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onSuccess
import io.element.android.libraries.push.impl.history.onUnableToResolveEvent
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.push.MutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.push.OnNotifiableEventReceived
import io.element.android.libraries.push.impl.push.OnRedactedEventReceived
import io.element.android.libraries.push.impl.push.SyncOnNotifiableEvent
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "NotifResultProcessor"

interface NotificationResultProcessor {
    suspend fun emit(results: Map<PushRequest, Result<ResolvedPushEvent>>)
    fun start()
    fun stop()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultNotificationResultProcessor(
    private val pushHistoryService: PushHistoryService,
    private val batteryOptimizationStore: MutableBatteryOptimizationStore,
    private val fallbackNotificationFactory: FallbackNotificationFactory,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val featureFlagService: FeatureFlagService,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
) : NotificationResultProcessor {
    private val resultFlow = MutableSharedFlow<Map<PushRequest, Result<ResolvedPushEvent>>>(extraBufferCapacity = Int.MAX_VALUE)
    private var processJob: Job? = null

    override suspend fun emit(results: Map<PushRequest, Result<ResolvedPushEvent>>) {
        resultFlow.emit(results)
    }

    override fun start() {
        if (processJob?.isActive == true) {
            Timber.tag(TAG).w("Is already processing, not starting again")
            return
        }
        processJob = resultFlow
            .onEach(::processResults)
            .launchIn(coroutineScope)
    }

    override fun stop() {
        if (processJob?.isActive != true) {
            Timber.tag(TAG).w("Is not processing, not stopping")
            return
        }

        processJob?.cancel()
        processJob = null
    }

    private suspend fun processResults(results: Map<PushRequest, Result<ResolvedPushEvent>>) {
        // TODO what happens with items that weren't reported back?
        for ((request, result) in results) {
            result.fold(
                onSuccess = {
                    if (it is ResolvedPushEvent.Event && it.notifiableEvent is FallbackNotifiableEvent) {
                        pushHistoryService.onUnableToResolveEvent(
                            providerInfo = request.providerInfo,
                            eventId = EventId(request.eventId),
                            roomId = RoomId(request.roomId),
                            sessionId = SessionId(request.sessionId),
                            reason = it.notifiableEvent.cause.orEmpty(),
                        )
                    } else {
                        pushHistoryService.onSuccess(
                            providerInfo = request.providerInfo,
                            eventId = EventId(request.eventId),
                            roomId = RoomId(request.roomId),
                            sessionId = SessionId(request.sessionId),
                            comment = "Push handled successfully",
                        )
                    }
                },
                onFailure = { exception ->
                    if (exception is NotificationResolverException.EventFilteredOut) {
                        pushHistoryService.onSuccess(
                            providerInfo = request.providerInfo,
                            eventId = EventId(request.eventId),
                            roomId = RoomId(request.roomId),
                            sessionId = SessionId(request.sessionId),
                            comment = "Push handled successfully but notification was filtered out",
                        )
                    } else if (exception is NotificationResolverException.EventRedacted) {
                        pushHistoryService.onSuccess(
                            providerInfo = request.providerInfo,
                            eventId = EventId(request.eventId),
                            roomId = RoomId(request.roomId),
                            sessionId = SessionId(request.sessionId),
                            comment = "Push handled successfully but event has been redacted",
                        )
                    } else {
                        val reason = when (exception) {
                            is NotificationResolverException.EventNotFound -> "Event not found"
                            else -> "Unknown error: ${exception.message}"
                        }
                        pushHistoryService.onUnableToResolveEvent(
                            providerInfo = request.providerInfo,
                            eventId = EventId(request.eventId),
                            roomId = RoomId(request.roomId),
                            sessionId = SessionId(request.sessionId),
                            reason = "$reason - Showing fallback notification",
                        )
                        batteryOptimizationStore.showBatteryOptimizationBanner()
                    }
                }
            )
        }

        val events = mutableListOf<NotifiableEvent>()
        val redactions = mutableListOf<ResolvedPushEvent.Redaction>()

        @Suppress("LoopWithTooManyJumpStatements")
        for ((request, result) in results) {
            val event = result.recover { exception ->
                // If the event could not be resolved, we create a fallback notification
                when (exception) {
                    is NotificationResolverException.EventFilteredOut -> {
                        // Do nothing, we don't want to show a notification for filtered out events
                        null
                    }
                    is NotificationResolverException.EventRedacted -> {
                        // Do nothing, we don't want to show a notification for redacted events
                        null
                    }
                    else -> {
                        Timber.tag(TAG).e(exception, "Failed to resolve push event")
                        ResolvedPushEvent.Event(
                            fallbackNotificationFactory.create(
                                sessionId = SessionId(request.sessionId),
                                roomId = RoomId(request.roomId),
                                eventId = EventId(request.eventId),
                                cause = exception.message,
                            )
                        )
                    }
                }
            }.getOrNull() ?: continue

            val userPushStore = userPushStoreFactory.getOrCreate(event.sessionId)
            val areNotificationsEnabled = userPushStore.getNotificationEnabledForDevice().first()
            // If notifications are disabled for this session and device, we don't want to show the notification
            // But if it's a ringing call, we want to show it anyway
            val isRingingCall = (event as? ResolvedPushEvent.Event)?.notifiableEvent is NotifiableRingingCallEvent
            if (!areNotificationsEnabled && !isRingingCall) continue

            // We categorise each result into either a NotifiableEvent or a Redaction
            when (event) {
                is ResolvedPushEvent.Event -> {
                    events.add(event.notifiableEvent)
                }
                is ResolvedPushEvent.Redaction -> {
                    redactions.add(event)
                }
            }
        }

        // Process redactions of messages in background to not block operations with higher priority
        if (redactions.isNotEmpty()) {
            coroutineScope.launch { onRedactedEventReceived.onRedactedEventsReceived(redactions) }
        }

        // Find and process ringing call notifications separately
        val (ringingCallEvents, nonRingingCallEvents) = events.partition { it is NotifiableRingingCallEvent }
        for (ringingCallEvent in ringingCallEvents) {
            Timber.tag(TAG).d("Ringing call event: $ringingCallEvent")
            handleRingingCallEvent(ringingCallEvent as NotifiableRingingCallEvent)
        }

        // Finally, process other notifications (messages, invites, generic notifications, etc.)
        if (nonRingingCallEvents.isNotEmpty()) {
            onNotifiableEventReceived.onNotifiableEventsReceived(nonRingingCallEvents)
        }

        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncNotificationsWithWorkManager)) {
            syncOnNotifiableEvent(results.keys.toList())
        }
    }

    private suspend fun handleRingingCallEvent(notifiableEvent: NotifiableRingingCallEvent) {
        Timber.i("## handleInternal() : Incoming call.")
        elementCallEntryPoint.handleIncomingCall(
            callType = CallType.RoomCall(
                notifiableEvent.sessionId,
                notifiableEvent.roomId,
                isAudioCall = notifiableEvent.callIntent == CallIntent.AUDIO
            ),
            eventId = notifiableEvent.eventId,
            senderId = notifiableEvent.senderId,
            roomName = notifiableEvent.roomName,
            senderName = notifiableEvent.senderDisambiguatedDisplayName,
            avatarUrl = notifiableEvent.roomAvatarUrl,
            timestamp = notifiableEvent.timestamp,
            expirationTimestamp = notifiableEvent.expirationTimestamp,
            notificationChannelId = notificationChannels.getChannelForIncomingCall(ring = true),
            textContent = notifiableEvent.description,
        )
    }
}
