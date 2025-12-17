/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onDiagnosticPush
import io.element.android.libraries.push.impl.history.onInvalidPushReceived
import io.element.android.libraries.push.impl.history.onSuccess
import io.element.android.libraries.push.impl.history.onUnableToResolveEvent
import io.element.android.libraries.push.impl.history.onUnableToRetrieveSession
import io.element.android.libraries.push.impl.notifications.FallbackNotificationFactory
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPushHandler(
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val incrementPushDataStore: IncrementPushDataStore,
    private val mutableBatteryOptimizationStore: MutableBatteryOptimizationStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
    private val pushHistoryService: PushHistoryService,
    private val resolverQueue: NotificationResolverQueue,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val fallbackNotificationFactory: FallbackNotificationFactory,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
    private val featureFlagService: FeatureFlagService,
) : PushHandler {
    init {
        processPushEventResults()
    }

    /**
     * Process the push notification event results emitted by the [resolverQueue].
     */
    private fun processPushEventResults() {
        resolverQueue.results
            .map { (requests, resolvedEvents) ->
                for (request in requests) {
                    // Log the result of the push notification event
                    val result = resolvedEvents[request]
                    if (result == null) {
                        pushHistoryService.onUnableToResolveEvent(
                            providerInfo = request.providerInfo,
                            eventId = request.eventId,
                            roomId = request.roomId,
                            sessionId = request.sessionId,
                            reason = "Push not handled: no result found for request",
                        )
                    } else {
                        result.fold(
                            onSuccess = {
                                if (it is ResolvedPushEvent.Event && it.notifiableEvent is FallbackNotifiableEvent) {
                                    pushHistoryService.onUnableToResolveEvent(
                                        providerInfo = request.providerInfo,
                                        eventId = request.eventId,
                                        roomId = request.roomId,
                                        sessionId = request.sessionId,
                                        reason = it.notifiableEvent.cause.orEmpty(),
                                    )
                                } else {
                                    pushHistoryService.onSuccess(
                                        providerInfo = request.providerInfo,
                                        eventId = request.eventId,
                                        roomId = request.roomId,
                                        sessionId = request.sessionId,
                                        comment = "Push handled successfully",
                                    )
                                }
                            },
                            onFailure = { exception ->
                                if (exception is NotificationResolverException.EventFilteredOut) {
                                    pushHistoryService.onSuccess(
                                        providerInfo = request.providerInfo,
                                        eventId = request.eventId,
                                        roomId = request.roomId,
                                        sessionId = request.sessionId,
                                        comment = "Push handled successfully but notification was filtered out",
                                    )
                                } else {
                                    val reason = when (exception) {
                                        is NotificationResolverException.EventNotFound -> "Event not found"
                                        else -> "Unknown error: ${exception.message}"
                                    }
                                    pushHistoryService.onUnableToResolveEvent(
                                        providerInfo = request.providerInfo,
                                        eventId = request.eventId,
                                        roomId = request.roomId,
                                        sessionId = request.sessionId,
                                        reason = "$reason - Showing fallback notification",
                                    )
                                    mutableBatteryOptimizationStore.showBatteryOptimizationBanner()
                                }
                            }
                        )
                    }
                }

                val events = mutableListOf<NotifiableEvent>()
                val redactions = mutableListOf<ResolvedPushEvent.Redaction>()

                @Suppress("LoopWithTooManyJumpStatements")
                for ((request, result) in resolvedEvents) {
                    val event = result.recover { exception ->
                        // If the event could not be resolved, we create a fallback notification
                        when (exception) {
                            is NotificationResolverException.EventFilteredOut -> {
                                // Do nothing, we don't want to show a notification for filtered out events
                                null
                            }
                            else -> {
                                Timber.tag(loggerTag.value).e(exception, "Failed to resolve push event")
                                ResolvedPushEvent.Event(
                                    fallbackNotificationFactory.create(
                                        sessionId = request.sessionId,
                                        roomId = request.roomId,
                                        eventId = request.eventId,
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
                    appCoroutineScope.launch { onRedactedEventReceived.onRedactedEventsReceived(redactions) }
                }

                // Find and process ringing call notifications separately
                val (ringingCallEvents, nonRingingCallEvents) = events.partition { it is NotifiableRingingCallEvent }
                for (ringingCallEvent in ringingCallEvents) {
                    Timber.tag(loggerTag.value).d("Ringing call event: $ringingCallEvent")
                    handleRingingCallEvent(ringingCallEvent as NotifiableRingingCallEvent)
                }

                // Finally, process other notifications (messages, invites, generic notifications, etc.)
                if (nonRingingCallEvents.isNotEmpty()) {
                    onNotifiableEventReceived.onNotifiableEventsReceived(nonRingingCallEvents)
                }

                if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncNotificationsWithWorkManager)) {
                    syncOnNotifiableEvent(requests)
                }
            }
            .launchIn(appCoroutineScope)
    }

    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     * @param providerInfo the provider info.
     */
    override suspend fun handle(pushData: PushData, providerInfo: String) {
        Timber.tag(loggerTag.value).d("## handling pushData: ${pushData.roomId}/${pushData.eventId}")
        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }
        incrementPushDataStore.incrementPushCounter()
        // Diagnostic Push
        if (pushData.eventId == DefaultTestPush.TEST_EVENT_ID) {
            pushHistoryService.onDiagnosticPush(providerInfo)
            diagnosticPushHandler.handlePush()
        } else {
            handleInternal(pushData, providerInfo)
        }
    }

    override suspend fun handleInvalid(providerInfo: String, data: String) {
        incrementPushDataStore.incrementPushCounter()
        pushHistoryService.onInvalidPushReceived(providerInfo, data)
    }

    /**
     * Internal receive method.
     *
     * @param pushData Object containing message data.
     * @param providerInfo the provider info.
     */
    private suspend fun handleInternal(pushData: PushData, providerInfo: String) {
        try {
            if (buildMeta.lowPrivacyLoggingEnabled) {
                Timber.tag(loggerTag.value).d("## handleInternal() : $pushData")
            } else {
                Timber.tag(loggerTag.value).d("## handleInternal()")
            }
            // Get userId from client secret
            val userId = pushClientSecret.getUserIdFromSecret(pushData.clientSecret)
            if (userId == null) {
                Timber.w("Unable to get userId from client secret")
                pushHistoryService.onUnableToRetrieveSession(
                    providerInfo = providerInfo,
                    eventId = pushData.eventId,
                    roomId = pushData.roomId,
                    reason = "Unable to get userId from client secret",
                )
                return
            }

            appCoroutineScope.launch {
                val notificationEventRequest = NotificationEventRequest(
                    sessionId = userId,
                    roomId = pushData.roomId,
                    eventId = pushData.eventId,
                    providerInfo = providerInfo,
                )
                Timber.d("Queueing notification: $notificationEventRequest")
                resolverQueue.enqueue(notificationEventRequest)
            }
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
        }
    }

    private suspend fun handleRingingCallEvent(notifiableEvent: NotifiableRingingCallEvent) {
        Timber.i("## handleInternal() : Incoming call.")
        elementCallEntryPoint.handleIncomingCall(
            callType = CallType.RoomCall(notifiableEvent.sessionId, notifiableEvent.roomId),
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
