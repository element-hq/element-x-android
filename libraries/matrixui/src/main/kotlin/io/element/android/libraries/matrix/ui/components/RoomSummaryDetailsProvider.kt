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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.user.MatrixUser

open class RoomSummaryDetailsProvider : PreviewParameterProvider<RoomSummary> {
    override val values: Sequence<RoomSummary>
        get() = sequenceOf(
            aRoomSummaryDetails(),
            aRoomSummaryDetails(name = null),
        )
}

fun aRoomSummaryDetails(
    roomId: RoomId = RoomId("!room:domain"),
    name: String? = "roomName",
    canonicalAlias: RoomAlias? = null,
    isDirect: Boolean = true,
    avatarUrl: String? = null,
    lastMessage: RoomMessage? = null,
    inviter: RoomMember? = null,
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    isDm: Boolean = false,
    numUnreadMentions: Int = 0,
    numUnreadMessages: Int = 0,
    numUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    isFavorite: Boolean = false,
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    heroes: List<MatrixUser> = emptyList(),
) = RoomSummary(
    roomId = roomId,
    name = name,
    canonicalAlias = canonicalAlias,
    isDirect = isDirect,
    avatarUrl = avatarUrl,
    lastMessage = lastMessage,
    inviter = inviter,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
    numUnreadMentions = numUnreadMentions,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    isFavorite = isFavorite,
    currentUserMembership = currentUserMembership,
    heroes = heroes,
)
