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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.toImmutableSet
import javax.inject.Inject

class CustomReactionPresenter @Inject constructor() : Presenter<CustomReactionState> {

    @Composable
    override fun present(): CustomReactionState {
        var selectedEvent by remember { mutableStateOf<TimelineItem.Event?>(null) }
        var emojiState: Async<EmojibaseStore> by remember {
            mutableStateOf(Async.Uninitialized)
        }
        val localCoroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        fun handleShowCustomReactionSheet(event: TimelineItem.Event) {
            selectedEvent = event
            emojiState = Async.Loading()
            localCoroutineScope.launch {
                emojiState = Async.Success(EmojibaseDatasource().load(context))
            }
        }

        fun handleDismissCustomReactionSheet() {
            selectedEvent = null
            emojiState = Async.Uninitialized
        }

        fun handleEvents(event: CustomReactionEvents) {
            when (event) {
                is CustomReactionEvents.ShowCustomReactionSheet -> handleShowCustomReactionSheet(event.event)
                is CustomReactionEvents.DismissCustomReactionSheet -> handleDismissCustomReactionSheet()
            }
        }
        val selectedEmoji = selectedEvent?.reactionsState?.reactions?.mapNotNull { if(it.isHighlighted) it.key else null }.orEmpty().toImmutableSet()
        return CustomReactionState(
            selectedEventId = selectedEvent?.eventId,
            emojiProvider = emojiState,
            selectedEmoji = selectedEmoji,
            eventSink = ::handleEvents
        )
    }
}
