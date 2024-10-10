/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.ui.model.InviteSender
import kotlinx.collections.immutable.toImmutableList

open class RoomListRoomSummaryProvider : PreviewParameterProvider<RoomListRoomSummary> {
    override val values: Sequence<RoomListRoomSummary>
        get() = sequenceOf(
            listOf(
                aRoomListRoomSummary(displayType = RoomSummaryDisplayType.PLACEHOLDER),
                aRoomListRoomSummary(),
                aRoomListRoomSummary(name = null),
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
                    isDm = true,
                ),
                aRoomListRoomSummary(
                    name = null,
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@bob:matrix.org"),
                        displayName = "Bob",
                    ),
                ),
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
    name: String? = "Room name",
    numberOfUnreadMessages: Long = 0,
    numberOfUnreadMentions: Long = 0,
    numberOfUnreadNotifications: Long = 0,
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
    heroes: List<AvatarData> = emptyList(),
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
    heroes = heroes.toImmutableList(),
)
