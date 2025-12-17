/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class ReportRoomPresenter(
    @Assisted private val roomId: RoomId,
    private val reportRoom: ReportRoom,
) : Presenter<ReportRoomState> {
    @AssistedFactory
    fun interface Factory {
        fun create(roomId: RoomId): ReportRoomPresenter
    }

    @Composable
    override fun present(): ReportRoomState {
        var reason by rememberSaveable { mutableStateOf("") }
        var leaveRoom by rememberSaveable { mutableStateOf(false) }
        var reportAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: ReportRoomEvents) {
            when (event) {
                ReportRoomEvents.Report -> coroutineScope.reportRoom(reason, leaveRoom, reportAction)
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
            reason = reason,
            leaveRoom = leaveRoom,
            reportAction = reportAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.reportRoom(
        reason: String,
        shouldLeave: Boolean,
        action: MutableState<AsyncAction<Unit>>
    ) = launch {
        val previousFailure = action.value as? AsyncAction.Failure
        val shouldReport = previousFailure?.error !is ReportRoom.Exception.LeftRoomFailed
        runUpdatingState(action) {
            reportRoom(
                roomId = roomId,
                shouldReport = shouldReport,
                reason = reason,
                shouldLeave = shouldLeave
            )
        }
    }
}
