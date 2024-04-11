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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import org.jetbrains.annotations.VisibleForTesting
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
        val roomInfo by matrixClient.getRoomInfoFlow(roomId).collectAsState(initial = Optional.empty())
        val contentState by produceState<AsyncData<ContentState>>(initialValue = AsyncData.Uninitialized, key1 = roomInfo) {
            value = when {
                roomInfo.isPresent -> {
                    val contentState = roomInfo.get().toContentState()
                    AsyncData.Success(contentState)
                }
                roomDescription.isPresent -> {
                    val contentState = roomDescription.get().toContentState()
                    AsyncData.Success(contentState)
                }
                else -> {
                    AsyncData.Uninitialized
                }
            }
        }
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        fun handleEvents(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.AcceptInvite, JoinRoomEvents.JoinRoom -> {
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

@VisibleForTesting
internal fun RoomDescription.toContentState(): ContentState {
    return ContentState(
        roomId = roomId,
        name = name,
        description = description,
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
    fun title(): String {
        return name ?: canonicalAlias ?: id
    }

    fun description(): String? {
        val topic = topic
        val alias = canonicalAlias
        val name = name
        return when {
            topic != null -> topic
            name != null && alias != null -> alias
            name == null && alias == null -> null
            else -> id
        }
    }

    return ContentState(
        roomId = RoomId(id),
        name = title(),
        description = description(),
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
internal fun AsyncData<ContentState>.toInviteData(): InviteData? {
    return dataOrNull()?.let { contentState ->
        InviteData(
            roomId = contentState.roomId,
            roomName = contentState.name,
            isDirect = contentState.isDirect
        )
    }
}
