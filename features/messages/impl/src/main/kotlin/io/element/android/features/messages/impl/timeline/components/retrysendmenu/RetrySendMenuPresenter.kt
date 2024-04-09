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

package io.element.android.features.messages.impl.timeline.components.retrysendmenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.launch
import javax.inject.Inject

class RetrySendMenuPresenter @Inject constructor(
    private val room: MatrixRoom,
) : Presenter<RetrySendMenuState> {
    @Composable
    override fun present(): RetrySendMenuState {
        val coroutineScope = rememberCoroutineScope()
        var selectedEvent: TimelineItem.Event? by remember { mutableStateOf(null) }

        fun handleEvent(event: RetrySendMenuEvents) {
            when (event) {
                is RetrySendMenuEvents.EventSelected -> {
                    selectedEvent = event.event
                }
                RetrySendMenuEvents.Retry -> {
                    coroutineScope.launch {
                        selectedEvent?.transactionId?.let { transactionId ->
                            room.retrySendMessage(transactionId)
                        }
                        selectedEvent = null
                    }
                }
                RetrySendMenuEvents.Remove -> {
                    coroutineScope.launch {
                        selectedEvent?.transactionId?.let { transactionId ->
                            room.cancelSend(transactionId)
                        }
                        selectedEvent = null
                    }
                }
                RetrySendMenuEvents.Dismiss -> {
                    selectedEvent = null
                }
            }
        }

        return RetrySendMenuState(
            selectedEvent = selectedEvent,
            eventSink = { handleEvent(it) },
        )
    }
}
