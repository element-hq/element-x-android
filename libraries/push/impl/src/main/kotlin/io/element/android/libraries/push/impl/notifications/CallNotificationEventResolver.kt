/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.RtcNotificationType
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

/**
 * Helper to resolve a valid [NotifiableEvent] from a [NotificationData].
 */
interface CallNotificationEventResolver {
    /**
     * Resolve a call notification event from a notification data depending on whether it should be a ringing one or not.
     * @param sessionId the current session id
     * @param notificationData the notification data
     * @param forceNotify `true` to force the notification to be non-ringing, `false` to use the default behaviour. Default is `false`.
     * @return a [NotifiableEvent] if the notification data is a call notification, null otherwise
     */
    suspend fun resolveEvent(
        sessionId: SessionId,
        notificationData: NotificationData,
        forceNotify: Boolean = false,
    ): Result<NotifiableEvent>
}

@ContributesBinding(AppScope::class)
class DefaultCallNotificationEventResolver(
    private val stringProvider: StringProvider,
    private val appForegroundStateService: AppForegroundStateService,
    private val clientProvider: MatrixClientProvider,
) : CallNotificationEventResolver {
    override suspend fun resolveEvent(
        sessionId: SessionId,
        notificationData: NotificationData,
        forceNotify: Boolean
    ): Result<NotifiableEvent> = runCatchingExceptions {
        val content = notificationData.content as? NotificationContent.MessageLike.RtcNotification
            ?: throw NotificationResolverException.UnknownError("content is not a call notify")

        val previousRingingCallStatus = appForegroundStateService.hasRingingCall.value
        // We need the sync service working to get the updated room info
        val isRoomCallActive = runCatchingExceptions {
            if (content.type == RtcNotificationType.RING) {
                appForegroundStateService.updateHasRingingCall(true)

                val client = clientProvider.getOrRestore(
                    sessionId
                ).getOrNull() ?: throw NotificationResolverException.UnknownError("Session $sessionId not found")
                val room = client.getRoom(
                    notificationData.roomId
                ) ?: throw NotificationResolverException.UnknownError("Room ${notificationData.roomId} not found")
                // Give a few seconds for the room info flow to catch up with the sync, if needed - this is usually instant
                val isActive = withTimeoutOrNull(3.seconds) { room.roomInfoFlow.firstOrNull { it.hasRoomCall } }?.hasRoomCall ?: false

                // We no longer need the sync service to be active because of a call notification.
                appForegroundStateService.updateHasRingingCall(previousRingingCallStatus)

                isActive
            } else {
                // If the call notification is not of ringing type, we don't need to check if the call is active
                false
            }
        }.onFailure {
            // Make sure to reset the hasRingingCall state in case of failure
            appForegroundStateService.updateHasRingingCall(previousRingingCallStatus)
        }.getOrDefault(false)

        notificationData.run {
            if (content.type == RtcNotificationType.RING && isRoomCallActive && !forceNotify) {
                NotifiableRingingCallEvent(
                    sessionId = sessionId,
                    roomId = roomId,
                    eventId = eventId,
                    roomName = roomDisplayName,
                    editedEventId = null,
                    canBeReplaced = true,
                    timestamp = this.timestamp,
                    isRedacted = false,
                    isUpdated = false,
                    description = stringProvider.getString(R.string.notification_incoming_call),
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    roomAvatarUrl = roomAvatarUrl,
                    rtcNotificationType = content.type,
                    senderId = content.senderId,
                    senderAvatarUrl = senderAvatarUrl,
                    expirationTimestamp = content.expirationTimestampMillis,
                )
            } else {
                Timber.d("Event $eventId is call notify but should not ring: $isRoomCallActive, notify: ${content.type}")
                // Create a simple message notification event
                buildNotifiableMessageEvent(
                    sessionId = sessionId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = true,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = stringProvider.getString(R.string.notification_incoming_call),
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    type = EventType.RTC_NOTIFICATION,
                )
            }
        }
    }
}
