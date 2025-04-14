/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.RoomId

class ReportRoomPresenter @AssistedInject constructor(
    @Assisted private val roomId: RoomId,
) : Presenter<ReportRoomState> {

    @AssistedFactory
    interface Factory {
        fun create(roomId: RoomId): ReportRoomPresenter
    }

    @Composable
    override fun present(): ReportRoomState {
        var reason by rememberSaveable { mutableStateOf("") }
        var leaveRoom by rememberSaveable { mutableStateOf(false) }
        var reportAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(event: ReportRoomEvents) {
            when (event) {
                ReportRoomEvents.Report -> TODO()
                ReportRoomEvents.ToggleLeaveRoom -> {
                    leaveRoom = !leaveRoom
                }
                is ReportRoomEvents.UpdateReason -> {
                    reason = event.reason
                }
                ReportRoomEvents.ClearReportAction -> {
                    reportAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return ReportRoomState(
            reason  = reason,
            leaveRoom = leaveRoom,
            reportAction = reportAction.value,
            eventSink = ::handleEvents
        )
    }
}
