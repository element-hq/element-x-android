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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.DmChecker
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.impl.notificationsettings.RoomNotificationSettingsMapper
import io.element.android.libraries.matrix.impl.room.elementHeroes
import io.element.android.libraries.matrix.impl.room.map
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.message.RoomMessageFactory
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.use

class RoomSummaryDetailsFactory(private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory()) {
    suspend fun create(roomListItem: RoomListItem): RoomSummaryDetails {
        val roomInfo = roomListItem.roomInfo()
        val latestRoomMessage = roomListItem.latestEvent()?.use {
            roomMessageFactory.create(it)
        }
        val isEncrypted = runCatching { roomListItem.isEncrypted() }.getOrDefault(false)
        return RoomSummaryDetails(
            roomId = RoomId(roomInfo.id),
            name = roomInfo.displayName,
            canonicalAlias = roomInfo.canonicalAlias?.let(::RoomAlias),
            isDirect = roomInfo.isDirect,
            avatarUrl = roomInfo.avatarUrl,
            numUnreadMentions = roomInfo.numUnreadMentions.toInt(),
            numUnreadMessages = roomInfo.numUnreadMessages.toInt(),
            numUnreadNotifications = roomInfo.numUnreadNotifications.toInt(),
            isMarkedUnread = roomInfo.isMarkedUnread,
            lastMessage = latestRoomMessage,
            inviter = roomInfo.inviter?.let(RoomMemberMapper::map),
            userDefinedNotificationMode = roomInfo.userDefinedNotificationMode?.let(RoomNotificationSettingsMapper::mapMode),
            hasRoomCall = roomInfo.hasRoomCall,
            // TODO find some way to get the encrypted info at this point. Right now we have to assume the room is encrypted to calculate if it's a DM
            isDm = DmChecker.isDm(direct = roomInfo.isDirect, activeMembersCount = roomInfo.activeMembersCount.toLong(), encrypted = isEncrypted),
            isFavorite = roomInfo.isFavourite,
            currentUserMembership = roomInfo.membership.map(),
            heroes = roomInfo.elementHeroes(),
        )
    }
}
