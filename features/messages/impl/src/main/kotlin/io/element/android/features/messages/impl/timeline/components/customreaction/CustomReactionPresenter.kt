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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomReactionPresenter @Inject constructor(
    private val emojibaseProvider: EmojibaseProvider,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<CustomReactionState> {
    @Composable
    override fun present(): CustomReactionState {
        val target: MutableState<CustomReactionState.Target> = remember {
            mutableStateOf(CustomReactionState.Target.None)
        }
        val skinTone by sessionPreferencesStore.getSkinTone().collectAsState(initial = null)

        val localCoroutineScope = rememberCoroutineScope()
        fun handleShowCustomReactionSheet(event: TimelineItem.Event) {
            target.value = CustomReactionState.Target.Loading(event)
            localCoroutineScope.launch {
                target.value = CustomReactionState.Target.Success(
                    event = event,
                    emojibaseStore = emojibaseProvider.emojibaseStore
                )
            }
        }

        fun handleDismissCustomReactionSheet() {
            target.value = CustomReactionState.Target.None
        }

        fun handleEvents(event: CustomReactionEvents) {
            when (event) {
                is CustomReactionEvents.ShowCustomReactionSheet -> handleShowCustomReactionSheet(event.event)
                is CustomReactionEvents.DismissCustomReactionSheet -> handleDismissCustomReactionSheet()
            }
        }
        val event = (target.value as? CustomReactionState.Target.Success)?.event
        val selectedEmoji = event
            ?.reactionsState
            ?.reactions
            ?.mapNotNull { if (it.isHighlighted) it.key else null }
            .orEmpty()
            .toImmutableSet()
        return CustomReactionState(
            target = target.value,
            selectedEmoji = selectedEmoji,
            skinTone = skinTone,
            eventSink = ::handleEvents,
        )
    }
}
