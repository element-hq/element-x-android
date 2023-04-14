/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("NotifiableEventResolver", pushLoggerTag)

/**
 * The notifiable event resolver is able to create a NotifiableEvent (view model for notifications) from an sdk Event.
 * It is used as a bridge between the Event Thread and the NotificationDrawerManager.
 * The NotifiableEventResolver is the only aware of session/store, the NotificationDrawerManager has no knowledge of that,
 * this pattern allow decoupling between the object responsible of displaying notifications and the matrix sdk.
 */
class NotifiableEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
    // private val noticeEventFormatter: NoticeEventFormatter,
    // private val displayableEventFormatter: DisplayableEventFormatter,
    private val clock: SystemClock,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val buildMeta: BuildMeta,
) {

    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent? {
        // Restore session
        val session = matrixAuthenticationService.restoreSession(sessionId).getOrNull() ?: return null
        // TODO EAx, no need for a session?
        val notificationData = session.let {// TODO Use make the app crashes
            it.notificationService().getNotification(
                userId = sessionId,
                roomId = roomId,
                eventId = eventId,
            )
        }.fold(
            {
                it
            },
            {
                Timber.tag(loggerTag.value).e(it, "Unable to resolve event.")
                null
            }
        ).orDefault(roomId, eventId)

        return notificationData.asNotifiableEvent(sessionId, roomId, eventId)
    }
}

private fun NotificationData.asNotifiableEvent(userId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent {
    return NotifiableMessageEvent(
        sessionId = userId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        canBeReplaced = true,
        noisy = false,
        timestamp = System.currentTimeMillis(),
        senderName = null,
        senderId = null,
        body = "$eventId in $roomId",
        imageUriString = null,
        threadId = null,
        roomName = null,
        roomIsDirect = false,
        roomAvatarPath = null,
        senderAvatarPath = null,
        soundName = null,
        outGoingMessage = false,
        outGoingMessageFailed = false,
        isRedacted = false,
        isUpdated = false
    )
}

/**
 * TODO This is a temporary method for EAx.
 */
private fun NotificationData?.orDefault(roomId: RoomId, eventId: EventId): NotificationData {
    return this ?: NotificationData(
        item = MatrixTimelineItem.Event(
            event = EventTimelineItem(
                uniqueIdentifier = eventId.value,
                eventId = eventId,
                isEditable = false,
                isLocal = false,
                isOwn = false,
                isRemote = false,
                localSendState = null,
                reactions = emptyList(),
                sender = UserId(""),
                senderProfile = ProfileTimelineDetails.Unavailable,
                timestamp = System.currentTimeMillis(),
                content = MessageContent(
                    body = eventId.value,
                    inReplyTo = null,
                    isEdited = false,
                    type = TextMessageType(
                        body = eventId.value,
                        formatted = null
                    )
                )
            ),
        ),
        title = roomId.value,
        subtitle = eventId.value,
        isNoisy = false,
        avatarUrl = null,
    )
}
