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

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import kotlinx.coroutines.launch
import java.util.Optional

class JoinRoomPresenter @AssistedInject constructor(
    @Assisted private val roomId: RoomId,
    @Assisted private val roomDescription: Optional<RoomDescription>,
    private val matrixClient: MatrixClient,
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
) : Presenter<JoinRoomState> {
    interface Factory {
        fun create(roomId: RoomId, roomDescription: Optional<RoomDescription>): JoinRoomPresenter
    }

    @Composable
    override fun present(): JoinRoomState {
        val coroutineScope = rememberCoroutineScope()
        val roomInfo by matrixClient.getRoomInfoFlow(roomId).collectAsState(initial = Optional.empty())
        val contentState by produceState<ContentState>(initialValue = ContentState.Loading(roomId), key1 = roomInfo) {
            value = when {
                roomInfo.isPresent -> {
                    roomInfo.get().toContentState()
                }
                roomDescription.isPresent -> {
                    roomDescription.get().toContentState()
                }
                else -> {
                    coroutineScope.launch {
                        val result = matrixClient.getRoomPreview(roomId.value)
                        value = result.getOrNull()
                            ?.toContentState()
                            ?: ContentState.UnknownRoom(roomId)
                    }
                    ContentState.Loading(roomId)
                }
            }
        }
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        fun handleEvents(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.AcceptInvite,
                JoinRoomEvents.JoinRoom -> {
                    val inviteData = contentState.toInviteData() ?: return
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                    )
                }
                JoinRoomEvents.DeclineInvite -> {
                    val inviteData = contentState.toInviteData() ?: return
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                    )
                }
            }
        }

        return JoinRoomState(
            contentState = contentState,
            acceptDeclineInviteState = acceptDeclineInviteState,
            eventSink = ::handleEvents
        )
    }
}

private fun RoomPreview.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = numberOfJoinedMembers,
        isDirect = false,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when {
            isInvited -> JoinAuthorisationStatus.IsInvited
            canKnock -> JoinAuthorisationStatus.CanKnock
            isPublic -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        }
    )
}

@VisibleForTesting
internal fun RoomDescription.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = alias,
        numberOfMembers = numberOfMembers,
        isDirect = false,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when (joinRule) {
            RoomDescription.JoinRule.KNOCK -> JoinAuthorisationStatus.CanKnock
            RoomDescription.JoinRule.PUBLIC -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        }
    )
}

@VisibleForTesting
internal fun MatrixRoomInfo.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = id,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = activeMembersCount,
        isDirect = isDirect,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when {
            currentUserMembership == CurrentUserMembership.INVITED -> JoinAuthorisationStatus.IsInvited
            isPublic -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        }
    )
}

@VisibleForTesting
internal fun ContentState.toInviteData(): InviteData? {
    return when (this) {
        is ContentState.Loaded -> InviteData(
            roomId = roomId,
            roomName = computedTitle,
            isDirect = isDirect
        )
        else -> null
    }
}
