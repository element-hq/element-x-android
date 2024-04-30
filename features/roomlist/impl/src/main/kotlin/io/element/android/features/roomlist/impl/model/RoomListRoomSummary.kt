/*
 * Copyright (c) 2022 New Vector Ltd
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

import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.ui.model.InviteSender

@Immutable
data class RoomListRoomSummary(
    val id: String,
    val displayType: RoomSummaryDisplayType,
    val roomId: RoomId,
    val name: String?,
    val canonicalAlias: RoomAlias?,
    val numberOfUnreadMessages: Int,
    val numberOfUnreadMentions: Int,
    val numberOfUnreadNotifications: Int,
    val isMarkedUnread: Boolean,
    val timestamp: String?,
    val lastMessage: CharSequence?,
    val avatarData: AvatarData,
    val userDefinedNotificationMode: RoomNotificationMode?,
    val hasRoomCall: Boolean,
    val isDirect: Boolean,
    val isDm: Boolean,
    val isFavorite: Boolean,
    val inviteSender: InviteSender?,
) {
    val isHighlighted = userDefinedNotificationMode != RoomNotificationMode.MUTE &&
        (numberOfUnreadNotifications > 0 || numberOfUnreadMentions > 0) ||
        isMarkedUnread ||
        displayType == RoomSummaryDisplayType.INVITE

    val hasNewContent = numberOfUnreadMessages > 0 ||
        numberOfUnreadMentions > 0 ||
        numberOfUnreadNotifications > 0 ||
        isMarkedUnread ||
        displayType == RoomSummaryDisplayType.INVITE
}
