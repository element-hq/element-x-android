/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.datasource

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.toInviteSender
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class RoomListRoomSummaryFactory @Inject constructor(
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
) {
    fun create(details: RoomSummary): RoomListRoomSummary {
        val avatarData = details.getAvatarData(size = AvatarSize.RoomListItem)
        return RoomListRoomSummary(
            id = details.roomId.value,
            roomId = details.roomId,
            name = details.name,
            numberOfUnreadMessages = details.numUnreadMessages,
            numberOfUnreadMentions = details.numUnreadMentions,
            numberOfUnreadNotifications = details.numUnreadNotifications,
            isMarkedUnread = details.isMarkedUnread,
            timestamp = lastMessageTimestampFormatter.format(details.lastMessageTimestamp),
            lastMessage = details.lastMessage?.let { message ->
                roomLastMessageFormatter.format(message.event, details.isDm)
            }.orEmpty(),
            avatarData = avatarData,
            userDefinedNotificationMode = details.userDefinedNotificationMode,
            hasRoomCall = details.hasRoomCall,
            isDirect = details.isDirect,
            isFavorite = details.isFavorite,
            inviteSender = details.inviter?.toInviteSender(),
            isDm = details.isDm,
            canonicalAlias = details.canonicalAlias,
            displayType = if (details.currentUserMembership == CurrentUserMembership.INVITED) {
                RoomSummaryDisplayType.INVITE
            } else {
                RoomSummaryDisplayType.ROOM
            },
            heroes = details.heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomListItem)
            }.toImmutableList(),
        )
    }
}
