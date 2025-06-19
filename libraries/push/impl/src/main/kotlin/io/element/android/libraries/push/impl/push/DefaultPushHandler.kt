/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onDiagnosticPush
import io.element.android.libraries.push.impl.history.onInvalidPushReceived
import io.element.android.libraries.push.impl.history.onSuccess
import io.element.android.libraries.push.impl.history.onUnableToResolveEvent
import io.element.android.libraries.push.impl.history.onUnableToRetrieveSession
import io.element.android.libraries.push.impl.notifications.NotificationEventRequest
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
import javax.inject.Inject

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPushHandler @Inject constructor(
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val incrementPushDataStore: IncrementPushDataStore,
    private val mutableBatteryOptimizationStore: MutableBatteryOptimizationStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
    private val pushHistoryService: PushHistoryService,
    private val resolverQueue: NotificationResolverQueue,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
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
                                        reason = "Showing fallback notification",
                                    )
                                    mutableBatteryOptimizationStore.showBatteryOptimizationBanner()
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
                                pushHistoryService.onUnableToResolveEvent(
                                    providerInfo = request.providerInfo,
                                    eventId = request.eventId,
                                    roomId = request.roomId,
                                    sessionId = request.sessionId,
                                    reason = exception.message ?: exception.javaClass.simpleName,
                                )
                                mutableBatteryOptimizationStore.showBatteryOptimizationBanner()
                            }
                        )
                    }
                }

                val events = mutableListOf<NotifiableEvent>()
                val redactions = mutableListOf<ResolvedPushEvent.Redaction>()

                @Suppress("LoopWithTooManyJumpStatements")
                for (result in resolvedEvents.values) {
                    val event = result.getOrNull() ?: continue
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

                // Process redactions of messages
                if (redactions.isNotEmpty()) {
                    onRedactedEventReceived.onRedactedEventsReceived(redactions)
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
            val clientSecret = pushData.clientSecret
            // clientSecret should not be null. If this happens, restore default session
            var reason = if (clientSecret == null) "No client secret" else ""
            val userId = clientSecret?.let {
                // Get userId from client secret
                pushClientSecret.getUserIdFromSecret(clientSecret).also {
                    if (it == null) {
                        reason = "Unable to get userId from client secret"
                    }
                }
            }
                ?: run {
                    matrixAuthenticationService.getLatestSessionId().also {
                        if (it == null) {
                            if (reason.isNotEmpty()) reason += " - "
                            reason += "Unable to get latest sessionId"
                        }
                    }
                }
            if (userId == null) {
                Timber.w("Unable to get a session")
                pushHistoryService.onUnableToRetrieveSession(
                    providerInfo = providerInfo,
                    eventId = pushData.eventId,
                    roomId = pushData.roomId,
                    reason = reason,
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
            notificationChannelId = notificationChannels.getChannelForIncomingCall(ring = true),
            textContent = notifiableEvent.description,
        )
    }
}
