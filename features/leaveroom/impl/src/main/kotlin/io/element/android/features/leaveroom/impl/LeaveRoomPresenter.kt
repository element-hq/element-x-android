/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.api.LeaveRoomState.Confirmation.Dm
import io.element.android.features.leaveroom.api.LeaveRoomState.Confirmation.Generic
import io.element.android.features.leaveroom.api.LeaveRoomState.Confirmation.LastUserInRoom
import io.element.android.features.leaveroom.api.LeaveRoomState.Confirmation.PrivateRoom
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.isDm
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LeaveRoomPresenter @Inject constructor(
    private val client: MatrixClient,
    private val roomMembershipObserver: RoomMembershipObserver,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<LeaveRoomState> {
    @Composable
    override fun present(): LeaveRoomState {
        val scope = rememberCoroutineScope()
        val confirmation = remember { mutableStateOf<LeaveRoomState.Confirmation>(LeaveRoomState.Confirmation.Hidden) }
        val progress = remember { mutableStateOf<LeaveRoomState.Progress>(LeaveRoomState.Progress.Hidden) }
        val error = remember { mutableStateOf<LeaveRoomState.Error>(LeaveRoomState.Error.Hidden) }

        return LeaveRoomState(
            confirmation = confirmation.value,
            progress = progress.value,
            error = error.value,
        ) { event ->
            when (event) {
                is LeaveRoomEvent.ShowConfirmation -> scope.launch(dispatchers.io) {
                    showLeaveRoomAlert(
                        matrixClient = client,
                        roomId = event.roomId,
                        confirmation = confirmation,
                    )
                }

                is LeaveRoomEvent.HideConfirmation -> confirmation.value = LeaveRoomState.Confirmation.Hidden
                is LeaveRoomEvent.LeaveRoom -> scope.launch(dispatchers.io) {
                    client.leaveRoom(
                        roomId = event.roomId,
                        roomMembershipObserver = roomMembershipObserver,
                        confirmation = confirmation,
                        progress = progress,
                        error = error,
                    )
                }

                is LeaveRoomEvent.HideError -> error.value = LeaveRoomState.Error.Hidden
            }
        }
    }
}

private suspend fun showLeaveRoomAlert(
    matrixClient: MatrixClient,
    roomId: RoomId,
    confirmation: MutableState<LeaveRoomState.Confirmation>,
) {
    matrixClient.getRoom(roomId)?.use { room ->
        confirmation.value = when {
            room.isDm -> Dm(roomId)
            !room.isPublic -> PrivateRoom(roomId)
            room.joinedMemberCount == 1L -> LastUserInRoom(roomId)
            else -> Generic(roomId)
        }
    }
}

private suspend fun MatrixClient.leaveRoom(
    roomId: RoomId,
    roomMembershipObserver: RoomMembershipObserver,
    confirmation: MutableState<LeaveRoomState.Confirmation>,
    progress: MutableState<LeaveRoomState.Progress>,
    error: MutableState<LeaveRoomState.Error>,
) {
    confirmation.value = LeaveRoomState.Confirmation.Hidden
    progress.value = LeaveRoomState.Progress.Shown
    getRoom(roomId)?.use { room ->
        room.leave().onSuccess {
            roomMembershipObserver.notifyUserLeftRoom(room.roomId)
        }.onFailure {
            Timber.e(it, "Error while leaving room ${room.displayName} - ${room.roomId}")
            error.value = LeaveRoomState.Error.Shown
        }
    }
    progress.value = LeaveRoomState.Progress.Hidden
}
