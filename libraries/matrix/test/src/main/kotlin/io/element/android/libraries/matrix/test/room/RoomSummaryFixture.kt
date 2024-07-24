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
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.user.MatrixUser
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
    numUnreadMentions: Int = 0,
    numUnreadMessages: Int = 0,
    notificationMode: RoomNotificationMode? = null,
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
) = aRoomSummary(
    roomId = roomId,
    name = name,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    numUnreadMentions = numUnreadMentions,
    numUnreadMessages = numUnreadMessages,
    notificationMode = notificationMode,
    currentUserMembership = currentUserMembership,
)

fun aRoomSummary(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = aRoomMessage(),
    numUnreadMentions: Int = 0,
    numUnreadMessages: Int = 0,
    numUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    notificationMode: RoomNotificationMode? = null,
    inviter: RoomMember? = null,
    canonicalAlias: RoomAlias? = null,
    hasRoomCall: Boolean = false,
    isDm: Boolean = false,
    isFavorite: Boolean = false,
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    heroes: List<MatrixUser> = emptyList(),
) = RoomSummary(
    roomId = roomId,
    name = name,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    numUnreadMentions = numUnreadMentions,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    userDefinedNotificationMode = notificationMode,
    inviter = inviter,
    canonicalAlias = canonicalAlias,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
    isFavorite = isFavorite,
    currentUserMembership = currentUserMembership,
    heroes = heroes,
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
