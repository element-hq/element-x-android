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
                contentState = AsyncData.Uninitialized
            ),
            aJoinRoomState(
                contentState = AsyncData.Success(
                    aContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin)
                )
            ),
            aJoinRoomState(
                contentState = AsyncData.Success(
                    aContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock)
                )
            ),
            aJoinRoomState(
                contentState = AsyncData.Success(
                    aContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited)
                )
            ),
        )
}

fun aContentState(
    roomId: RoomId = RoomId("@exa:matrix.org"),
    name: String = "Element x android",
    description: String? = "#exa:matrix.org",
    numberOfMembers: Long? = null,
    isDirect: Boolean = false,
    roomAvatarUrl: String? = null,
    joinAuthorisationStatus: JoinAuthorisationStatus = JoinAuthorisationStatus.Unknown
) = ContentState(
    roomId = roomId,
    name = name,
    description = description,
    numberOfMembers = numberOfMembers,
    isDirect = isDirect,
    roomAvatarUrl = roomAvatarUrl,
    joinAuthorisationStatus = joinAuthorisationStatus
)

fun aJoinRoomState(
    contentState: AsyncData<ContentState> = AsyncData.Success(
        aContentState()
    ),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    eventSink: (JoinRoomEvents) -> Unit = {}
) = JoinRoomState(
    contentState = contentState,
    acceptDeclineInviteState = acceptDeclineInviteState,
    eventSink = eventSink
)

