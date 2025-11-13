/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import kotlinx.coroutines.launch

@Inject
class HistoryVisibleStatePresenter(
    private val repository: HistoryVisibleAcknowledgementRepository,
    private val room: JoinedRoom,
) : Presenter<HistoryVisibleState> {
    @Composable
    override fun present(): HistoryVisibleState {
        val roomInfo by room.roomInfoFlow.collectAsState()
        // Implicitly acknowledge the initial event to avoid flashes in UI.
        val acknowledged by repository.hasAcknowledged(room.roomId).collectAsState(initial = true)

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(roomInfo.historyVisibility) {
            if (roomInfo.historyVisibility == RoomHistoryVisibility.Joined && acknowledged) {
                repository.setAcknowledged(room.roomId, false)
            }
        }

        return HistoryVisibleState(
            showAlert = roomInfo.historyVisibility != RoomHistoryVisibility.Joined && roomInfo.isEncrypted == true && !acknowledged,
            eventSink = { event ->
                when (event) {
                    is HistoryVisibleEvent.Acknowledge ->
                        coroutineScope.launch {
                            repository.setAcknowledged(room.roomId, true)
                        }
                }
            }
        )
    }
}
