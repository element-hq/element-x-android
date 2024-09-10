/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fixtures

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallNotifyType
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent

fun aSimpleNotifiableEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    type: String? = null,
    isRedacted: Boolean = false,
    canBeReplaced: Boolean = false,
    editedEventId: EventId? = null
) = SimpleNotifiableEvent(
    sessionId = sessionId,
    roomId = roomId,
    eventId = eventId,
    editedEventId = editedEventId,
    noisy = false,
    title = "title",
    description = "description",
    type = type,
    timestamp = 0,
    soundName = null,
    canBeReplaced = canBeReplaced,
    isRedacted = isRedacted
)

fun anInviteNotifiableEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    isRedacted: Boolean = false
) = InviteNotifiableEvent(
    sessionId = sessionId,
    eventId = eventId,
    roomId = roomId,
    roomName = "a room name",
    editedEventId = null,
    noisy = false,
    title = "title",
    description = "description",
    type = null,
    timestamp = 0,
    soundName = null,
    canBeReplaced = false,
    isRedacted = isRedacted
)

fun aNotifiableMessageEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    threadId: ThreadId? = null,
    isRedacted: Boolean = false,
    timestamp: Long = 0,
    type: String = EventType.MESSAGE,
) = NotifiableMessageEvent(
    sessionId = sessionId,
    eventId = eventId,
    editedEventId = null,
    noisy = false,
    timestamp = timestamp,
    senderDisambiguatedDisplayName = "sender-name",
    senderId = UserId("@sending-id:domain.com"),
    body = "message-body",
    roomId = roomId,
    threadId = threadId,
    roomName = "room-name",
    canBeReplaced = false,
    isRedacted = isRedacted,
    imageUriString = null,
    type = type,
)

fun aNotifiableCallEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    senderId: UserId = A_USER_ID_2,
    senderName: String? = null,
    roomAvatarUrl: String? = AN_AVATAR_URL,
    senderAvatarUrl: String? = AN_AVATAR_URL,
    callNotifyType: CallNotifyType = CallNotifyType.NOTIFY,
    timestamp: Long = 0L,
) = NotifiableRingingCallEvent(
    sessionId = sessionId,
    eventId = eventId,
    roomId = roomId,
    roomName = "a room name",
    editedEventId = null,
    description = "description",
    timestamp = timestamp,
    canBeReplaced = false,
    isRedacted = false,
    isUpdated = false,
    senderDisambiguatedDisplayName = senderName,
    senderId = senderId,
    roomAvatarUrl = roomAvatarUrl,
    senderAvatarUrl = senderAvatarUrl,
    callNotifyType = callNotifyType,
)
