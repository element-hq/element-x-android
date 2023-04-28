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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class RoomDetailsPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val room: MatrixRoom,
    private val roomMembershipObserver: RoomMembershipObserver,
) : Presenter<RoomDetailsState> {

    private val roomMemberDetailsPresenter by lazy {
        val dmMember = runBlocking {
            room.getDmMember().firstOrNull()
        }
        if (dmMember != null) {
            RoomMemberDetailsPresenter(matrixClient.sessionId, room, dmMember)
        } else {
            null
        }
    }

    @Composable
    override fun present(): RoomDetailsState {
        val coroutineScope = rememberCoroutineScope()
        var leaveRoomWarning by remember {
            mutableStateOf<LeaveRoomWarning?>(null)
        }
        var error by remember {
            mutableStateOf<RoomDetailsError?>(null)
        }

        val memberCount by produceState<Async<Int>>(initialValue = Async.Loading(null)) {
            room.members().map { it.count() }
                .onEach { value = Async.Success(it) }
                .catch { value = Async.Failure(it) }
                .launchIn(coroutineScope)
        }

        val dmMember by room.getDmMember().collectAsState(initial = null)
        val roomType = if (dmMember != null) {
            RoomDetailsType.Dm(dmMember!!)
        } else {
            RoomDetailsType.Room
        }

        fun handleEvents(event: RoomDetailsEvent) {
            when (event) {
                is RoomDetailsEvent.LeaveRoom -> {
                    if (event.needsConfirmation) {
                        leaveRoomWarning = LeaveRoomWarning.computeLeaveRoomWarning(room.isPublic, memberCount)
                    } else {
                        coroutineScope.launch(Dispatchers.IO) {
                            room.leave()
                                .onSuccess {
                                    roomMembershipObserver.notifyUserLeftRoom(room.roomId)
                                }.onFailure {
                                    error = RoomDetailsError.AlertGeneric
                                }
                            leaveRoomWarning = null
                        }
                    }
                }
                is RoomDetailsEvent.ClearLeaveRoomWarning -> leaveRoomWarning = null
                RoomDetailsEvent.ClearError -> error = null
            }
        }

        val roomMemberDetailsState = if (dmMember != null) {
            roomMemberDetailsPresenter?.present()
        } else {
            null
        }

        return RoomDetailsState(
            roomId = room.roomId.value,
            roomName = room.name ?: room.displayName,
            roomAlias = room.alias,
            roomAvatarUrl = room.avatarUrl,
            roomTopic = room.topic,
            memberCount = memberCount,
            isEncrypted = room.isEncrypted,
            displayLeaveRoomWarning = leaveRoomWarning,
            error = error,
            roomType = roomType,
            roomMemberDetailsState = roomMemberDetailsState,
            eventSink = ::handleEvents,
        )
    }
}
