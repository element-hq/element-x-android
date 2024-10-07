/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.leaveroom.fake

import androidx.compose.runtime.Composable
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.architecture.Presenter

class FakeLeaveRoomPresenter : Presenter<LeaveRoomState> {
    val events = mutableListOf<LeaveRoomEvent>()

    private fun handleEvent(event: LeaveRoomEvent) {
        events += event
    }

    private var state = LeaveRoomState(
        confirmation = LeaveRoomState.Confirmation.Hidden,
        progress = LeaveRoomState.Progress.Hidden,
        error = LeaveRoomState.Error.Hidden,
        eventSink = ::handleEvent,
    )
        set(value) {
            field = value.copy(eventSink = ::handleEvent)
        }

    fun givenState(state: LeaveRoomState) {
        this.state = state
    }

    @Composable
    override fun present(): LeaveRoomState = state
}
