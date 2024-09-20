/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.notificationsettings.RoomNotificationSettingsMapper
import io.element.android.libraries.matrix.impl.room.elementHeroes
import io.element.android.libraries.matrix.impl.room.map
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.message.RoomMessageFactory
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.use

class RoomSummaryDetailsFactory(
    private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory(),
) {
    suspend fun create(roomListItem: RoomListItem): RoomSummary {
        val roomInfo = roomListItem.roomInfo()
        val latestRoomMessage = roomListItem.latestEvent().use { event ->
            roomMessageFactory.create(event)
        }
        return RoomSummary(
            roomId = RoomId(roomInfo.id),
            name = roomInfo.displayName,
            canonicalAlias = roomInfo.canonicalAlias?.let(::RoomAlias),
            alternativeAliases = roomInfo.alternativeAliases.map(::RoomAlias),
            isDirect = roomInfo.isDirect,
            avatarUrl = roomInfo.avatarUrl,
            numUnreadMentions = roomInfo.numUnreadMentions.toInt(),
            numUnreadMessages = roomInfo.numUnreadMessages.toInt(),
            numUnreadNotifications = roomInfo.numUnreadNotifications.toInt(),
            isMarkedUnread = roomInfo.isMarkedUnread,
            lastMessage = latestRoomMessage,
            inviter = roomInfo.inviter?.let(RoomMemberMapper::map),
            userDefinedNotificationMode = roomInfo.cachedUserDefinedNotificationMode?.let(RoomNotificationSettingsMapper::mapMode),
            hasRoomCall = roomInfo.hasRoomCall,
            isDm = isDm(isDirect = roomInfo.isDirect, activeMembersCount = roomInfo.activeMembersCount.toInt()),
            isFavorite = roomInfo.isFavourite,
            currentUserMembership = roomInfo.membership.map(),
            heroes = roomInfo.elementHeroes(),
        )
    }
}
