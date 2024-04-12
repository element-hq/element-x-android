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

package io.element.android.features.joinroom.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId

@Immutable
data class JoinRoomState(
    val contentState: ContentState,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val eventSink: (JoinRoomEvents) -> Unit
) {
    val joinAuthorisationStatus = when(contentState) {
        is ContentState.Loaded -> contentState.joinAuthorisationStatus
        else -> JoinAuthorisationStatus.Unknown
    }
}

sealed interface ContentState {
    data class Loading(val roomId: RoomId) : ContentState
    data class UnknownRoom(val roomId: RoomId) : ContentState
    data class Loaded(
        val roomId: RoomId,
        val name: String?,
        val topic: String?,
        val alias: String?,
        val numberOfMembers: Long?,
        val isDirect: Boolean,
        val roomAvatarUrl: String?,
        val joinAuthorisationStatus: JoinAuthorisationStatus,
    ) : ContentState {
        val computedTitle = name ?: roomId.value

        val computedSubtitle = when {
            alias != null -> alias
            name == null -> ""
            else -> roomId.value
        }

        val showMemberCount = numberOfMembers != null

        fun avatarData(size: AvatarSize): AvatarData {
            return AvatarData(
                id = roomId.value,
                name = name,
                url = roomAvatarUrl,
                size = size,
            )
        }
    }
}

enum class JoinAuthorisationStatus {
    IsInvited,
    CanKnock,
    CanJoin,
    Unknown,
}
