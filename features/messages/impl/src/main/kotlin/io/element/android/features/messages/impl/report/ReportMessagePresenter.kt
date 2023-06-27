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

package io.element.android.features.messages.impl.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.SnackbarMessage
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.element.android.libraries.ui.strings.R as StringR

class ReportMessagePresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    @Assisted private val inputs: Inputs,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<ReportMessageState> {

    data class Inputs(
        val eventId: EventId,
        val senderId: UserId,
    )

    @AssistedFactory
    interface Factory {
        fun create(inputs: Inputs): ReportMessagePresenter
    }

    @Composable
    override fun present(): ReportMessageState {
        val coroutineScope = rememberCoroutineScope()
        var reason by rememberSaveable { mutableStateOf("") }
        var blockUser by rememberSaveable { mutableStateOf(false) }
        var result: MutableState<Async<Unit>> = remember { mutableStateOf(Async.Uninitialized) }

        fun handleEvents(event: ReportMessageEvents) {
            when (event) {
                is ReportMessageEvents.UpdateReason -> reason = event.reason
                ReportMessageEvents.ToggleBlockUser -> blockUser = !blockUser
                ReportMessageEvents.Report -> coroutineScope.report(inputs.eventId, inputs.senderId, reason, blockUser, result)
                ReportMessageEvents.ClearError -> result.value = Async.Uninitialized
            }
        }

        return ReportMessageState(
            reason = reason,
            blockUser = blockUser,
            result = result.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.report(
        eventId: EventId,
        userId: UserId,
        reason: String,
        blockUser: Boolean,
        result: MutableState<Async<Unit>>,
    ) = launch {
        result.runUpdatingState {
            val userIdToBlock = userId.takeIf { blockUser }
            room.reportContent(eventId, reason, userIdToBlock)
                .onSuccess {
                    snackbarDispatcher.post(SnackbarMessage(StringR.string.common_report_submitted))
                }
        }
    }
}
