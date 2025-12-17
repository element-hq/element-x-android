/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.recentemojis.api.EmojibaseProvider
import io.element.android.libraries.recentemojis.api.GetRecentEmojis
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch

@Inject
class CustomReactionPresenter(
    private val emojibaseProvider: EmojibaseProvider,
    private val getRecentEmojis: GetRecentEmojis,
) : Presenter<CustomReactionState> {
    @Composable
    override fun present(): CustomReactionState {
        val localCoroutineScope = rememberCoroutineScope()
        var recentEmojis by remember { mutableStateOf<ImmutableList<String>>(persistentListOf()) }

        val target: MutableState<CustomReactionState.Target> = remember {
            mutableStateOf(CustomReactionState.Target.None)
        }

        fun handleShowCustomReactionSheet(event: TimelineItem.Event) {
            target.value = CustomReactionState.Target.Loading(event)
            localCoroutineScope.launch {
                recentEmojis = getRecentEmojis().getOrNull() ?: persistentListOf()
                target.value = CustomReactionState.Target.Success(
                    event = event,
                    emojibaseStore = emojibaseProvider.emojibaseStore
                )
            }
        }

        fun handleDismissCustomReactionSheet() {
            target.value = CustomReactionState.Target.None
        }

        fun handleEvent(event: CustomReactionEvents) {
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
            recentEmojis = recentEmojis,
            eventSink = ::handleEvent,
        )
    }
}
