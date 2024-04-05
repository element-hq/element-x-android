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

package io.element.android.appnav.room.join

import androidx.compose.runtime.Immutable
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId

@Immutable
data class JoinRoomState(
    val roomInfo: AsyncData<RoomInfo>,
    val joinAuthorisationStatus: JoinAuthorisationStatus,
    val currentAction: CurrentAction,
    val eventSink: (JoinRoomEvents) -> Unit
){
    val showMemberCount = roomInfo.dataOrNull()?.memberCount != null
}

data class RoomInfo(
    val roomId: RoomId,
    val roomName: String,
    val roomAlias: String?,
    val memberCount: Long?,
    val roomAvatarUrl: String?,
) {
    fun avatarData(size: AvatarSize): AvatarData {
        return AvatarData(
            id = roomId.value,
            name = roomName,
            url = roomAvatarUrl,
            size = size,
        )
    }
}

enum class JoinAuthorisationStatus {
    IsInvited,
    CanKnock,
    CanJoin,
    Unknown,
}

sealed interface CurrentAction {
    data object None : CurrentAction
}
