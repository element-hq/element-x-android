/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.features.call.api.CallData
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.features.ptt.api.PttRoomService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.launch

/**
 * Stage 1 PTT prototype presenter.
 *
 * Drives Element Call as a push-to-talk "channel": the channel is just the room's MatrixRTC
 * audio session. Live-channel state (who is connected) is observed natively and reactively off
 * [JoinedRoom.roomInfoFlow] — no WebView required. Joining is done programmatically via
 * [ElementCallEntryPoint.startCall] with [CallData.isAudioCall] = true.
 *
 * NOTE: at this stage the microphone stays full-duplex (everyone hot). True half-duplex
 * push-to-transmit needs a microphone toggle on Element Call's `controls` API (Stage 2).
 */
@Inject
class PttPrototypePresenter(
    private val room: JoinedRoom,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val sessionEnterpriseService: SessionEnterpriseService,
    private val pttRoomService: PttRoomService,
) : Presenter<PttPrototypeState> {
    @Composable
    override fun present(): PttPrototypeState {
        val coroutineScope = rememberCoroutineScope()
        val isPttAvailable by produceState(false) {
            value = sessionEnterpriseService.isElementCallAvailable()
        }
        val isPttEnabled by pttRoomService.isPttEnabledFlow().collectAsState(initial = false)
        val roomInfo by room.roomInfoFlow.collectAsState()
        val hasLiveChannel by remember { derivedStateOf { roomInfo.hasRoomCall } }
        val participantCount by remember { derivedStateOf { roomInfo.activeRoomCallParticipants.size } }
        val isUserInChannel by remember {
            derivedStateOf { room.sessionId in roomInfo.activeRoomCallParticipants }
        }

        fun handleEvent(event: PttPrototypeEvent) {
            when (event) {
                PttPrototypeEvent.JoinPttChannel -> {
                    elementCallEntryPoint.startCall(
                        CallData(
                            sessionId = room.sessionId,
                            roomId = room.roomId,
                            isAudioCall = true,
                        )
                    )
                }
                is PttPrototypeEvent.SetPttEnabled -> {
                    coroutineScope.launch { pttRoomService.setPttEnabled(event.enabled) }
                }
            }
        }

        return PttPrototypeState(
            isPttAvailable = isPttAvailable,
            isPttEnabled = isPttEnabled,
            hasLiveChannel = hasLiveChannel,
            participantCount = participantCount,
            isUserInChannel = isUserInChannel,
            eventSink = ::handleEvent,
        )
    }
}
