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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId

open class JoinRoomStateProvider : PreviewParameterProvider<JoinRoomState> {
    override val values: Sequence<JoinRoomState>
        get() = sequenceOf(
            aJoinRoomState(
                roomInfo = AsyncData.Uninitialized
            ),
            aJoinRoomState(
                joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin
            ),
            aJoinRoomState(
                joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock
            ),
            aJoinRoomState(
                joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited
            ),
        )
}

fun aJoinRoomState(
    roomInfo: AsyncData<RoomInfo> = AsyncData.Success(
        RoomInfo(
            roomId = RoomId("@exa:matrix.org"),
            roomName = "Element x android",
            roomAlias = "#exa:matrix.org",
            memberCount = null,
            isDirect = false,
            roomAvatarUrl = null
        )
    ),
    joinAuthorisationStatus: JoinAuthorisationStatus = JoinAuthorisationStatus.Unknown,
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    eventSink: (JoinRoomEvents) -> Unit = {}
) = JoinRoomState(
    roomInfo = roomInfo,
    joinAuthorisationStatus = joinAuthorisationStatus,
    acceptDeclineInviteState = acceptDeclineInviteState,
    eventSink = eventSink
)

