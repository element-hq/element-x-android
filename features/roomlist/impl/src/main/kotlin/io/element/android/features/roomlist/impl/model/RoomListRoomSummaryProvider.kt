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
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

open class RoomListRoomSummaryProvider : PreviewParameterProvider<RoomListRoomSummary> {
    override val values: Sequence<RoomListRoomSummary>
        get() = sequenceOf(
            listOf(
                aRoomListRoomSummary(displayType = RoomSummaryDisplayType.PLACEHOLDER),
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
            listOf(
                aRoomListRoomSummary(
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@alice:matrix.org"),
                        displayName = "Alice",
                    ),
                    canonicalAlias = RoomAlias("#alias:matrix.org"),
                ),
                aRoomListRoomSummary(
                    name = "Bob",
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@bob:matrix.org"),
                        displayName = "Bob",
                    ),
                    isDirect = true,
                )
            ),
        ).flatten()
}

internal fun anInviteSender(
    userId: UserId = UserId("@bob:domain"),
    displayName: String = "Bob",
    avatarData: AvatarData = AvatarData(userId.value, displayName, size = AvatarSize.InviteSender),
) = InviteSender(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
)

internal fun aRoomListRoomSummary(
    id: String = "!roomId:domain",
    name: String = "Room name",
    numberOfUnreadMessages: Int = 0,
    numberOfUnreadMentions: Int = 0,
    numberOfUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    lastMessage: String? = "Last message",
    timestamp: String? = lastMessage?.let { "88:88" },
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    avatarData: AvatarData = AvatarData(id, name, size = AvatarSize.RoomListItem),
    isDirect: Boolean = false,
    isDm: Boolean = false,
    isFavorite: Boolean = false,
    inviteSender: InviteSender? = null,
    displayType: RoomSummaryDisplayType = RoomSummaryDisplayType.ROOM,
    canonicalAlias: RoomAlias? = null,
) = RoomListRoomSummary(
    id = id,
    roomId = RoomId(id),
    name = name,
    numberOfUnreadMessages = numberOfUnreadMessages,
    numberOfUnreadMentions = numberOfUnreadMentions,
    numberOfUnreadNotifications = numberOfUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    timestamp = timestamp,
    lastMessage = lastMessage,
    avatarData = avatarData,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDirect = isDirect,
    isDm = isDm,
    isFavorite = isFavorite,
    inviteSender = inviteSender,
    displayType = displayType,
    canonicalAlias = canonicalAlias,
)
