/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeNotificationResolverQueue(
    private val processingLambda: suspend (NotificationEventRequest) -> Result<ResolvedPushEvent> = {
        Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent()))
    },
) : NotificationResolverQueue {
    override val results = MutableSharedFlow<Pair<List<NotificationEventRequest>, Map<NotificationEventRequest, Result<ResolvedPushEvent>>>>(replay = 1)

    override suspend fun enqueue(request: NotificationEventRequest) {
        results.emit(listOf(request) to mapOf(request to processingLambda(request)))
    }
}

fun aNotifiableMessageEvent(
    body: String = "A message",
    sessionId: SessionId = SessionId("@alice:matrix.org"),
    roomId: RoomId = RoomId("!roomid:matrix.org"),
    eventId: EventId = EventId("\$event_id"),
    threadId: ThreadId? = null,
    isRedacted: Boolean = false,
    hasMentionOrReply: Boolean = false,
    timestamp: Long = 0L,
    type: String = EventType.MESSAGE,
    senderId: UserId = UserId("@bob:matrix.org"),
    senderDisambiguatedDisplayName: String = "Bob",
    roomName: String? = "A room",
) = NotifiableMessageEvent(
    sessionId = sessionId,
    eventId = eventId,
    editedEventId = null,
    noisy = false,
    timestamp = timestamp,
    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
    senderId = senderId,
    body = body,
    roomId = roomId,
    threadId = threadId,
    roomName = roomName,
    canBeReplaced = false,
    isRedacted = isRedacted,
    imageUriString = null,
    imageMimeType = null,
    roomAvatarPath = null,
    senderAvatarPath = null,
    soundName = null,
    outGoingMessage = false,
    outGoingMessageFailed = false,
    isUpdated = false,
    hasMentionOrReply = hasMentionOrReply,
    type = type,
)
