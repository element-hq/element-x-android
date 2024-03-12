/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.datasource

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import javax.inject.Inject

class RoomListRoomSummaryFactory @Inject constructor(
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
) {
    companion object {
        fun createPlaceholder(id: String): RoomListRoomSummary {
            return RoomListRoomSummary(
                id = id,
                roomId = RoomId(id),
                isPlaceholder = true,
                name = "Short name",
                timestamp = "hh:mm",
                lastMessage = "Last message for placeholder",
                avatarData = AvatarData(id, "S", size = AvatarSize.RoomListItem),
                numberOfUnreadMessages = 0,
                numberOfUnreadMentions = 0,
                numberOfUnreadNotifications = 0,
                isMarkedUnread = false,
                userDefinedNotificationMode = null,
                hasRoomCall = false,
                isDm = false,
                isFavorite = false,
            )
        }
    }

    fun create(roomSummary: RoomSummary.Filled): RoomListRoomSummary {
        val roomIdentifier = roomSummary.identifier()
        val avatarData = AvatarData(
            id = roomIdentifier,
            name = roomSummary.details.name,
            url = roomSummary.details.avatarUrl,
            size = AvatarSize.RoomListItem,
        )
        return RoomListRoomSummary(
            id = roomIdentifier,
            roomId = RoomId(roomIdentifier),
            name = roomSummary.details.name,
            numberOfUnreadMessages = roomSummary.details.numUnreadMessages,
            numberOfUnreadMentions = roomSummary.details.numUnreadMentions,
            numberOfUnreadNotifications = roomSummary.details.numUnreadNotifications,
            isMarkedUnread = roomSummary.details.isMarkedUnread,
            timestamp = lastMessageTimestampFormatter.format(roomSummary.details.lastMessageTimestamp),
            lastMessage = roomSummary.details.lastMessage?.let { message ->
                roomLastMessageFormatter.format(message.event, roomSummary.details.isDirect)
            }.orEmpty(),
            avatarData = avatarData,
            isPlaceholder = false,
            userDefinedNotificationMode = roomSummary.details.userDefinedNotificationMode,
            hasRoomCall = roomSummary.details.hasRoomCall,
            isDm = roomSummary.details.isDm,
            isFavorite = roomSummary.details.isFavorite,
        )
    }
}
