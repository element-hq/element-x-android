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

package io.element.android.features.roomlist.impl.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

open class RoomListRoomSummaryProvider : PreviewParameterProvider<RoomListRoomSummary> {
    override val values: Sequence<RoomListRoomSummary>
        get() = sequenceOf(
            aRoomListRoomSummary(),
            aRoomListRoomSummary().copy(lastMessage = null),
            aRoomListRoomSummary().copy(hasUnread = true, notificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY),
            aRoomListRoomSummary().copy(timestamp = "88:88", notificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY),
            aRoomListRoomSummary().copy(timestamp = "88:88", notificationMode = RoomNotificationMode.MUTE),
            aRoomListRoomSummary().copy(timestamp = "88:88", hasUnread = true),
            aRoomListRoomSummary().copy(isPlaceholder = true, timestamp = "88:88"),
            aRoomListRoomSummary().copy(
                name = "A very long room name that should be truncated",
                lastMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea com" +
                    "modo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                timestamp = "yesterday",
                hasUnread = true,
            ),
            aRoomListRoomSummary().copy(hasUnread = true, hasOngoingCall = true),
        )
}

fun aRoomListRoomSummary() = RoomListRoomSummary(
    id = "!roomId",
    roomId = RoomId("!roomId:domain"),
    name = "Room name",
    hasUnread = false,
    timestamp = null,
    lastMessage = "Last message",
    avatarData = AvatarData("!roomId", "Room name", size = AvatarSize.RoomListItem),
    isPlaceholder = false,
)
