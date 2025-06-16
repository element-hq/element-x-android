/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomcall.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.ui.room.canCall
import javax.inject.Inject

class RoomCallStatePresenter @Inject constructor(
    private val room: JoinedRoom,
    private val currentCallService: CurrentCallService,
    private val enterpriseService: EnterpriseService,
) : Presenter<RoomCallState> {
    @Composable
    override fun present(): RoomCallState {
        val isAvailable by produceState(false) {
            value = enterpriseService.isElementCallAvailable()
        }
        val roomInfo by room.roomInfoFlow.collectAsState()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canJoinCall by room.canCall(updateKey = syncUpdateFlow.value)
        val isUserInTheCall by remember {
            derivedStateOf {
                room.sessionId in roomInfo.activeRoomCallParticipants
            }
        }
        val currentCall by currentCallService.currentCall.collectAsState()
        val isUserLocallyInTheCall by remember {
            derivedStateOf {
                (currentCall as? CurrentCall.RoomCall)?.roomId == room.roomId
            }
        }
        val callState by remember {
            derivedStateOf {
                when {
                    isAvailable.not() -> RoomCallState.Unavailable
                    roomInfo.hasRoomCall -> RoomCallState.OnGoing(
                        canJoinCall = canJoinCall,
                        isUserInTheCall = isUserInTheCall,
                        isUserLocallyInTheCall = isUserLocallyInTheCall,
                    )
                    else -> RoomCallState.StandBy(canStartCall = canJoinCall)
                }
            }
        }
        return callState
    }
}
