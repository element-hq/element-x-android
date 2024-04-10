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
import io.element.android.features.invite.api.response.AcceptDeclineInvitePresenter
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class JoinRoomPresenter @AssistedInject constructor(
    @Assisted private val roomId: RoomId,
    @Assisted private val roomDescription: Optional<RoomDescription>,
    private val matrixClient: MatrixClient,
    private val acceptDeclineInvitePresenter: AcceptDeclineInvitePresenter,
) : Presenter<JoinRoomState> {

    interface Factory {
        fun create(roomId: RoomId, roomDescription: Optional<RoomDescription>): JoinRoomPresenter
    }

    @Composable
    override fun present(): JoinRoomState {
        val roomInfo by matrixClient.getRoomInfoFlow(roomId).collectAsState(initial = Optional.empty())
        val joinAuthorisationStatus = joinAuthorisationStatus(roomInfo)
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()
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
                else -> AsyncData.Uninitialized
            }
        }

        fun handleEvents(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.AcceptInvite, JoinRoomEvents.JoinRoom -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(contentState.toInviteData())
                    )
                }
                JoinRoomEvents.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(contentState.toInviteData())
                    )
                }
            }
        }

        return JoinRoomState(
            contentState = contentState,
            joinAuthorisationStatus = joinAuthorisationStatus,
            acceptDeclineInviteState = acceptDeclineInviteState,
            eventSink = ::handleEvents
        )
    }

    private fun RoomDescription.toContentState(): ContentState {
        return ContentState(
            roomId = roomId,
            name = name,
            description = description,
            numberOfMembers = numberOfMembers,
            isDirect = false,
            roomAvatarUrl = avatarUrl
        )
    }

    private fun MatrixRoomInfo.toContentState(): ContentState {
        fun title(): String {
            return name ?: canonicalAlias ?: roomId.value
        }

        fun description(): String? {
            val topic = topic
            val alias = canonicalAlias
            val name = name
            return when {
                topic != null -> topic
                name != null && alias != null -> alias
                name == null && alias == null -> null
                else -> roomId.value
            }
        }

        return ContentState(
            roomId = roomId,
            name = title(),
            description = description(),
            numberOfMembers = activeMembersCount,
            isDirect = isDirect,
            roomAvatarUrl = avatarUrl
        )
    }

    private fun AsyncData<ContentState>.toInviteData(): InviteData {
        return dataOrNull().let {
            InviteData(
                roomId = roomId,
                roomName = it?.name ?: "",
                isDirect = it?.isDirect ?: false
            )
        }
    }

    @Composable
    private fun joinAuthorisationStatus(roomInfo: Optional<MatrixRoomInfo>): JoinAuthorisationStatus {
        val userMembership = roomInfo.getOrNull()?.currentUserMembership
        return when {
            userMembership == CurrentUserMembership.INVITED -> return JoinAuthorisationStatus.IsInvited
            else -> JoinAuthorisationStatus.Unknown
        }
    }
}
