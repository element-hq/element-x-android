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

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDetails
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.EventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME

fun aRoomSummaryFilled(
    roomId: RoomId = A_ROOM_ID,
    name: String = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarURLString: String? = null,
    lastMessage: RoomMessage? = aRoomMessage(),
    lastMessageTimestamp: Long? = null,
    unreadNotificationCount: Int = 2,
) = RoomSummary.Filled(
    aRoomSummaryDetail(
        roomId = roomId,
        name = name,
        isDirect = isDirect,
        avatarURLString = avatarURLString,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadNotificationCount = unreadNotificationCount,
    )
)

fun aRoomSummaryDetail(
    roomId: RoomId = A_ROOM_ID,
    name: String = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarURLString: String? = null,
    lastMessage: RoomMessage? = aRoomMessage(),
    lastMessageTimestamp: Long? = null,
    unreadNotificationCount: Int = 2,
) = RoomSummaryDetails(
    roomId = roomId,
    name = name,
    isDirect = isDirect,
    avatarURLString = avatarURLString,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    unreadNotificationCount = unreadNotificationCount,
)

fun aRoomMessage(
    eventId: EventId = AN_EVENT_ID,
    event: EventTimelineItem = anEventTimelineItem(),
    userId: UserId = A_USER_ID,
    timestamp: Long = 0L,
) = RoomMessage(
    eventId = eventId,
    event = event,
    sender = userId,
    originServerTs = timestamp,
)

fun anEventTimelineItem(
    uniqueIdentifier: String = A_UNIQUE_ID,
    eventId: EventId = AN_EVENT_ID,
    isEditable: Boolean = false,
    isLocal: Boolean = false,
    isOwn: Boolean = false,
    isRemote: Boolean = false,
    localSendState: EventSendState? = null,
    reactions: List<EventReaction> = emptyList(),
    sender: UserId = A_USER_ID,
    senderProfile: ProfileTimelineDetails = aProfileTimelineDetails(),
    timestamp: Long = 0L,
    content: EventContent = aProfileChangeMessageContent(),
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
) = EventTimelineItem(
    uniqueIdentifier = uniqueIdentifier,
    eventId = eventId,
    isEditable = isEditable,
    isLocal = isLocal,
    isOwn = isOwn,
    isRemote = isRemote,
    localSendState = localSendState,
    reactions = reactions,
    sender = sender,
    senderProfile = senderProfile,
    timestamp = timestamp,
    content = content,
    debugInfo = debugInfo,
)

fun aProfileTimelineDetails(
    displayName: String? = A_USER_NAME,
    displayNameAmbiguous: Boolean = false,
    avatarUrl: String? = null
): ProfileTimelineDetails = ProfileTimelineDetails.Ready(
    displayName = displayName,
    displayNameAmbiguous = displayNameAmbiguous,
    avatarUrl = avatarUrl,
)

fun aProfileChangeMessageContent(
    displayName: String? = null,
    prevDisplayName: String? = null,
    avatarUrl: String? = null,
    prevAvatarUrl: String? = null,
) = ProfileChangeContent(
    displayName = displayName,
    prevDisplayName = prevDisplayName,
    avatarUrl = avatarUrl,
    prevAvatarUrl = prevAvatarUrl,
)

fun aTimelineItemDebugInfo(
    model: String = "Rust(Model())",
    originalJson: String? = null,
    latestEditedJson: String? = null,
) = TimelineItemDebugInfo(
    model, originalJson, latestEditedJson
)
