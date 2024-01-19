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

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.impl.notificationsettings.RoomNotificationSettingsMapper
import io.element.android.libraries.matrix.impl.room.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.message.RoomMessageFactory
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.use

class RoomSummaryDetailsFactory(private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory()) {
    fun create(roomInfo: RoomInfo): RoomSummaryDetails {
        val latestRoomMessage = roomInfo.latestEvent?.use {
            roomMessageFactory.create(it)
        }
        return RoomSummaryDetails(
            roomId = RoomId(roomInfo.id),
            name = roomInfo.name ?: roomInfo.id,
            canonicalAlias = roomInfo.canonicalAlias,
            isDirect = roomInfo.isDirect,
            avatarUrl = roomInfo.avatarUrl,
            unreadNotificationCount = roomInfo.notificationCount.toInt(),
            lastMessage = latestRoomMessage,
            inviter = roomInfo.inviter?.let(RoomMemberMapper::map),
            userDefinedNotificationMode = roomInfo.userDefinedNotificationMode?.let(RoomNotificationSettingsMapper::mapMode),
            hasRoomCall = roomInfo.hasRoomCall,
            isDm = roomInfo.isDirect && roomInfo.activeMembersCount.toLong() == 2L,
        )
    }
}
