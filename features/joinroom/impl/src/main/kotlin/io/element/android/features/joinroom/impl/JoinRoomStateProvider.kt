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
import io.element.android.libraries.matrix.api.core.RoomId

open class JoinRoomStateProvider : PreviewParameterProvider<JoinRoomState> {
    override val values: Sequence<JoinRoomState>
        get() = sequenceOf(
            aJoinRoomState(
                contentState = anUninitializedContentState()
            ),
            aJoinRoomState(
                contentState = anUnknownContentState()
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin)
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock)
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited)
            ),
        )
}

fun anUnknownContentState(roomId: RoomId = RoomId("@exa:matrix.org")) = ContentState.UnknownRoom(roomId)

fun anUninitializedContentState(roomId: RoomId = RoomId("@exa:matrix.org")) = ContentState.Loading(roomId)

fun aLoadedContentState(
    roomId: RoomId = RoomId("@exa:matrix.org"),
    name: String = "Element x android",
    alias: String? = "#exa:matrix.org",
    topic: String? = "Element X is a secure, private and decentralized messenger.",
    numberOfMembers: Long? = null,
    isDirect: Boolean = false,
    roomAvatarUrl: String? = null,
    joinAuthorisationStatus: JoinAuthorisationStatus = JoinAuthorisationStatus.Unknown
) = ContentState.Loaded(
    roomId = roomId,
    name = name,
    alias = alias,
    topic = topic,
    numberOfMembers = numberOfMembers,
    isDirect = isDirect,
    roomAvatarUrl = roomAvatarUrl,
    joinAuthorisationStatus = joinAuthorisationStatus
)

fun aJoinRoomState(
    contentState: ContentState = aLoadedContentState(),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    eventSink: (JoinRoomEvents) -> Unit = {}
) = JoinRoomState(
    contentState = contentState,
    acceptDeclineInviteState = acceptDeclineInviteState,
    eventSink = eventSink
)

