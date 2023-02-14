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

package io.element.android.libraries.matrixtest.room

import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.matrix.room.RoomSummary
import io.element.android.libraries.matrix.room.RoomSummaryDetails
import io.element.android.libraries.matrixtest.A_MESSAGE
import io.element.android.libraries.matrixtest.A_ROOM_ID
import io.element.android.libraries.matrixtest.A_ROOM_NAME

fun aRoomSummaryFilled(
    roomId: RoomId = A_ROOM_ID,
    name: String = A_ROOM_NAME,
    isDirect: Boolean = false,
    avatarURLString: String? = null,
    lastMessage: CharSequence? = A_MESSAGE,
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
    lastMessage: CharSequence? = A_MESSAGE,
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
