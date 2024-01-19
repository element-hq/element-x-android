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
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem

fun aRoomSummaryFilled(
    roomId: RoomId = A_ROOM_ID,
    name: String = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = aRoomMessage(),
    unreadNotificationCount: Int = 2,
    notificationMode: RoomNotificationMode? = null,
) = RoomSummary.Filled(
    aRoomSummaryDetails(
        roomId = roomId,
        name = name,
        isDirect = isDirect,
        avatarUrl = avatarUrl,
        lastMessage = lastMessage,
        unreadNotificationCount = unreadNotificationCount,
        notificationMode = notificationMode,
    )
)

fun aRoomSummaryDetails(
    roomId: RoomId = A_ROOM_ID,
    name: String = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = aRoomMessage(),
    unreadNotificationCount: Int = 2,
    notificationMode: RoomNotificationMode? = null,
) = RoomSummaryDetails(
    roomId = roomId,
    name = name,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    unreadNotificationCount = unreadNotificationCount,
    userDefinedNotificationMode = notificationMode
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
