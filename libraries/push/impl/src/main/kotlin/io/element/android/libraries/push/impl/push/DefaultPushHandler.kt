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
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onDiagnosticPush
import io.element.android.libraries.push.impl.history.onInvalidPushReceived
import io.element.android.libraries.push.impl.history.onSuccess
import io.element.android.libraries.push.impl.history.onUnableToResolveEvent
import io.element.android.libraries.push.impl.history.onUnableToRetrieveSession
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPushHandler @Inject constructor(
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val notifiableEventResolver: NotifiableEventResolver,
    private val incrementPushDataStore: IncrementPushDataStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
    private val pushHistoryService: PushHistoryService,
) : PushHandler {
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

    override suspend fun handleInvalid(providerInfo: String) {
        incrementPushDataStore.incrementPushCounter()
        pushHistoryService.onInvalidPushReceived(providerInfo)
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
            notifiableEventResolver.resolveEvent(userId, pushData.roomId, pushData.eventId).fold(
                onSuccess = { resolvedPushEvent ->
                    pushHistoryService.onSuccess(
                        providerInfo = providerInfo,
                        eventId = pushData.eventId,
                        roomId = pushData.roomId,
                        sessionId = userId,
                        comment = resolvedPushEvent.javaClass.simpleName,
                    )

                    when (resolvedPushEvent) {
                        is ResolvedPushEvent.Event -> {
                            when (val notifiableEvent = resolvedPushEvent.notifiableEvent) {
                                is NotifiableRingingCallEvent -> {
                                    onNotifiableEventReceived.onNotifiableEventReceived(notifiableEvent)
                                    handleRingingCallEvent(notifiableEvent)
                                }
                                else -> {
                                    val userPushStore = userPushStoreFactory.getOrCreate(userId)
                                    val areNotificationsEnabled = userPushStore.getNotificationEnabledForDevice().first()
                                    if (areNotificationsEnabled) {
                                        onNotifiableEventReceived.onNotifiableEventReceived(notifiableEvent)
                                    } else {
                                        Timber.tag(loggerTag.value).i("Notification are disabled for this device, ignore push.")
                                    }
                                }
                            }
                        }
                        is ResolvedPushEvent.Redaction -> {
                            onRedactedEventReceived.onRedactedEventReceived(resolvedPushEvent)
                        }
                    }
                },
                onFailure = { failure ->
                    Timber.tag(loggerTag.value).w(failure, "Unable to get a notification data")
                    pushHistoryService.onUnableToResolveEvent(
                        providerInfo = providerInfo,
                        eventId = pushData.eventId,
                        roomId = pushData.roomId,
                        sessionId = userId,
                        reason = failure.message ?: failure.javaClass.simpleName,
                    )
                }
            )
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
