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

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.ui.room.getDirectRoomMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomDetailsPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val roomMembershipObserver: RoomMembershipObserver,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMembersDetailsPresenterFactory: RoomMemberDetailsPresenter.Factory,
) : Presenter<RoomDetailsState> {

    @Composable
    override fun present(): RoomDetailsState {
        val coroutineScope = rememberCoroutineScope()
        val leaveRoomWarning = remember {
            mutableStateOf<LeaveRoomWarning?>(null)
        }
        val error = remember {
            mutableStateOf<RoomDetailsError?>(null)
        }
        LaunchedEffect(Unit) {
            room.updateMembers()
        }

        val membersState by room.membersStateFlow.collectAsState()
        val memberCount by getMemberCount(membersState)
        val dmMember by room.getDirectRoomMember(membersState)
        val roomMemberDetailsPresenter = roomMemberDetailsPresenter(dmMember)
        val roomType = getRoomType(dmMember)

        fun handleEvents(event: RoomDetailsEvent) {
            when (event) {
                is RoomDetailsEvent.LeaveRoom -> {
                    coroutineScope.leaveRoom(
                        needsConfirmation = event.needsConfirmation,
                        memberCount = memberCount,
                        leaveRoomWarning = leaveRoomWarning,
                        error = error,
                    )
                }
                is RoomDetailsEvent.ClearLeaveRoomWarning -> leaveRoomWarning.value = null
                RoomDetailsEvent.ClearError -> error.value = null
            }
        }

        val roomMemberDetailsState = roomMemberDetailsPresenter?.present()

        return RoomDetailsState(
            roomId = room.roomId.value,
            roomName = room.name ?: room.displayName,
            roomAlias = room.alias,
            roomAvatarUrl = room.avatarUrl,
            roomTopic = room.topic,
            memberCount = memberCount,
            isEncrypted = room.isEncrypted,
            displayLeaveRoomWarning = leaveRoomWarning.value,
            error = error.value,
            roomType = roomType.value,
            roomMemberDetailsState = roomMemberDetailsState,
            eventSink = ::handleEvents,
        )
    }

    @Composable
    private fun roomMemberDetailsPresenter(dmMemberState: RoomMember?) = remember(dmMemberState) {
        dmMemberState?.let { roomMember ->
            roomMembersDetailsPresenterFactory.create(roomMember.userId)
        }
    }

    @Composable
    private fun getRoomType(dmMember: RoomMember?): State<RoomDetailsType> = remember(dmMember) {
        derivedStateOf {
            if (dmMember != null) {
                RoomDetailsType.Dm(dmMember)
            } else {
                RoomDetailsType.Room
            }
        }
    }

    @Composable
    private fun getMemberCount(membersState: MatrixRoomMembersState): State<Async<Int>> {
        return remember(membersState) {
            derivedStateOf {
                when (membersState) {
                    MatrixRoomMembersState.Unknown -> Async.Uninitialized
                    is MatrixRoomMembersState.Pending -> Async.Loading(prevState = membersState.prevRoomMembers?.size)
                    is MatrixRoomMembersState.Error -> Async.Failure(membersState.failure, prevState = membersState.prevRoomMembers?.size)
                    is MatrixRoomMembersState.Ready -> Async.Success(membersState.roomMembers.filter { it.membership == RoomMembershipState.JOIN }.size)
                }
            }
        }
    }

    private fun CoroutineScope.leaveRoom(
        needsConfirmation: Boolean,
        memberCount: Async<Int>,
        leaveRoomWarning: MutableState<LeaveRoomWarning?>,
        error: MutableState<RoomDetailsError?>,
    ) = launch(coroutineDispatchers.io) {
        if (needsConfirmation) {
            leaveRoomWarning.value = LeaveRoomWarning.computeLeaveRoomWarning(room.isPublic, memberCount)
        } else {
            room.leave()
                .onSuccess {
                    roomMembershipObserver.notifyUserLeftRoom(room.roomId)
                }.onFailure {
                    error.value = RoomDetailsError.AlertGeneric
                }
            leaveRoomWarning.value = null
        }
    }
}




