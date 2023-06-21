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

package io.element.android.features.invitelist.impl.model

import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
data class InviteListInviteSummary(
    val roomId: RoomId,
    val roomName: String = "",
    val roomAlias: String? = null,
    val roomAvatarData: AvatarData = AvatarData(roomId.value, roomName, size = AvatarSize.RoomListItem),
    val sender: InviteSender? = null,
    val isDirect: Boolean = false,
    val isNew: Boolean = false,
)

data class InviteSender constructor(
    val userId: UserId,
    val displayName: String,
    val avatarData: AvatarData = AvatarData(userId.value, displayName, size = AvatarSize.InviteSender),
)
