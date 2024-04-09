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
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class JoinRoomPresenter @AssistedInject constructor(
    @Assisted private val roomId: RoomId,
    private val matrixClient: MatrixClient,
    private val roomListService: RoomListService,
    private val acceptDeclineInvitePresenter: AcceptDeclineInvitePresenter,
) : Presenter<JoinRoomState> {

    interface Factory {
        fun create(roomId: RoomId): JoinRoomPresenter
    }

    @Composable
    override fun present(): JoinRoomState {
        val userMembership by roomListService.getUserMembershipForRoom(roomId).collectAsState(initial = Optional.empty())
        val joinAuthorisationStatus = joinAuthorisationStatus(userMembership)
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()
        val roomInfo by produceState<AsyncData<RoomInfo>>(initialValue = AsyncData.Uninitialized, key1 = userMembership) {
            value = when {
                userMembership.isPresent -> {
                    val roomInfo = matrixClient.getRoom(roomId)?.use {
                        RoomInfo(
                            roomId = it.roomId,
                            roomName = it.displayName,
                            roomAlias = it.alias,
                            memberCount = it.activeMemberCount,
                            isDirect = it.isDirect,
                            roomAvatarUrl = it.avatarUrl
                        )
                    }
                    roomInfo?.let { AsyncData.Success(it) } ?: AsyncData.Failure(Exception("Failed to load room info"))
                }
                else -> AsyncData.Uninitialized
            }
        }

        fun handleEvents(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.AcceptInvite, JoinRoomEvents.JoinRoom -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(roomInfo.toInviteData())
                    )
                }
                JoinRoomEvents.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(roomInfo.toInviteData())
                    )
                }
            }
        }

        return JoinRoomState(
            roomInfo = roomInfo,
            joinAuthorisationStatus = joinAuthorisationStatus,
            acceptDeclineInviteState = acceptDeclineInviteState,
            eventSink = ::handleEvents
        )
    }

    private fun AsyncData<RoomInfo>.toInviteData(): InviteData {
        return dataOrNull().let {
            InviteData(
                roomId = roomId,
                roomName = it?.roomName ?: "",
                isDirect = it?.isDirect ?: false
            )
        }
    }

    @Composable
    private fun joinAuthorisationStatus(userMembership: Optional<CurrentUserMembership>): JoinAuthorisationStatus {
        return when {
            userMembership.getOrNull() == CurrentUserMembership.INVITED -> return JoinAuthorisationStatus.IsInvited
            else -> JoinAuthorisationStatus.Unknown
        }
    }
}
