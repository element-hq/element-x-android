/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomcall.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.canCall
import javax.inject.Inject

class RoomCallStatePresenter @Inject constructor(
    private val room: MatrixRoom,
    private val currentCallService: CurrentCallService,
) : Presenter<RoomCallState> {
    @Composable
    override fun present(): RoomCallState {
        val roomInfo by room.roomInfoFlow.collectAsState(null)
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canJoinCall by room.canCall(updateKey = syncUpdateFlow.value)
        val isUserInTheCall by remember {
            derivedStateOf {
                room.sessionId in roomInfo?.activeRoomCallParticipants.orEmpty()
            }
        }
        val currentCall by currentCallService.currentCall.collectAsState()
        val isUserLocallyInTheCall by remember {
            derivedStateOf {
                (currentCall as? CurrentCall.RoomCall)?.roomId == room.roomId
            }
        }
        val callState = when {
            roomInfo?.hasRoomCall == true -> RoomCallState.OnGoing(
                canJoinCall = canJoinCall,
                isUserInTheCall = isUserInTheCall,
                isUserLocallyInTheCall = isUserLocallyInTheCall,
            )
            else -> RoomCallState.StandBy(canStartCall = canJoinCall)
        }
        return callState
    }
}
