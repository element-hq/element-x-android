/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.utils.EmojiHistoryStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import javax.inject.Inject

class CustomReactionPresenter @Inject constructor(
    private val emojibaseProvider: EmojibaseProvider,
    private val emojiHistoryStore: EmojiHistoryStore,
) : Presenter<CustomReactionState> {
    @Composable
    override fun present(): CustomReactionState {
        val target: MutableState<CustomReactionState.Target> = remember {
            mutableStateOf(CustomReactionState.Target.None)
        }

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
                is CustomReactionEvents.AddEmojiToRecentlyUsed -> localCoroutineScope.launch {
                    emojiHistoryStore.add(event.emoji)
                }
            }
        }
        val event = (target.value as? CustomReactionState.Target.Success)?.event
        val selectedEmoji = event
            ?.reactionsState
            ?.reactions
            ?.mapNotNull { if (it.isHighlighted) it.key else null }
            .orEmpty()
            .toImmutableSet()

        val recentlyUsedEmojis by produceState(persistentListOf()) {
            emojiHistoryStore.getAll()
                .collect { map ->
                    value = map.entries
                        .sortedByDescending { it.value }
                        .map { it.key }
                        .take(60)
                        .toPersistentList()
                }
        }

        return CustomReactionState(
            target = target.value,
            selectedEmoji = selectedEmoji,
            recentlyUsedEmojis = recentlyUsedEmojis,
            eventSink = { handleEvents(it) }
        )
    }
}
