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

package io.element.android.features.leaveroom.fake

import androidx.compose.runtime.Composable
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.api.LeaveRoomState

class FakeLeaveRoomPresenter : LeaveRoomPresenter {
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
