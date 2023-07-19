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

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import javax.inject.Inject

class CustomReactionPresenter @Inject constructor() : Presenter<CustomReactionState> {

    @Composable
    override fun present(): CustomReactionState {
        var selectedEventId by remember { mutableStateOf<EventId?>(null) }

        fun handleEvents(event: CustomReactionEvents) {
            when (event) {
                is CustomReactionEvents.UpdateSelectedEvent -> selectedEventId = event.eventId
            }
        }

        return CustomReactionState(selectedEventId = selectedEventId, eventSink = ::handleEvents)
    }
}
