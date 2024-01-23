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
            listOf(
                aRoomListRoomSummary(isPlaceholder = true),
                aRoomListRoomSummary(),
                aRoomListRoomSummary(lastMessage = null),
                aRoomListRoomSummary(
                    name = "A very long room name that should be truncated",
                    lastMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                        " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea com" +
                        "modo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                    timestamp = "yesterday",
                    numberOfUnreadMessages = 1,
                ),
            ),
            listOf(false, true).map { hasCall ->
                listOf(
                    RoomNotificationMode.ALL_MESSAGES,
                    RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
                    RoomNotificationMode.MUTE,
                ).map { roomNotificationMode ->
                    listOf(
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            lastMessage = "No activity" + if (hasCall) ", call" else "",
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 0,
                            numberOfUnreadMentions = 0,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            lastMessage = "New messages" + if (hasCall) ", call" else "",
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 1,
                            numberOfUnreadMentions = 0,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            lastMessage = "New messages, mentions" + if (hasCall) ", call" else "",
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 1,
                            numberOfUnreadMentions = 1,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            lastMessage = "New mentions" + if (hasCall) ", call" else "",
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 0,
                            numberOfUnreadMentions = 1,
                            hasRoomCall = hasCall,
                        ),
                    )
                }.flatten()
            }.flatten(),
        ).flatten()
}

internal fun aRoomListRoomSummary(
    id: String = "!roomId:domain",
    name: String = "Room name",
    numberOfUnreadMessages: Int = 0,
    numberOfUnreadMentions: Int = 0,
    lastMessage: String? = "Last message",
    timestamp: String? = lastMessage?.let { "88:88" },
    isPlaceholder: Boolean = false,
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    avatarData: AvatarData = AvatarData(id, name, size = AvatarSize.RoomListItem),
    isDm: Boolean = false,
) = RoomListRoomSummary(
    id = id,
    roomId = RoomId(id),
    name = name,
    numberOfUnreadMessages = numberOfUnreadMessages,
    numberOfUnreadMentions = numberOfUnreadMentions,
    timestamp = timestamp,
    lastMessage = lastMessage,
    avatarData = avatarData,
    isPlaceholder = isPlaceholder,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
)
