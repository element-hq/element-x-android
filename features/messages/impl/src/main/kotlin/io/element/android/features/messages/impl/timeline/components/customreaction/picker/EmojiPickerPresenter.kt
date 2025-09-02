/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class EmojiPickerPresenter(
    private val emojibaseStore: EmojibaseStore,
) : Presenter<EmojiPickerState> {
    @Composable
    override fun present(): EmojiPickerState {
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var emojiResults by remember { mutableStateOf<SearchBarResultState<ImmutableList<Emoji>>>(SearchBarResultState.Initial()) }
        val categories = remember { emojibaseStore.categories }

        LaunchedEffect(searchQuery) {
            emojiResults = if (searchQuery.isEmpty()) {
                SearchBarResultState.Initial()
            } else {
                // Add a small delay to avoid doing too many computations when the user is typing quickly
                delay(100.milliseconds)

                val lowercaseQuery = searchQuery.lowercase()
                val results = withContext(Dispatchers.Default) {
                    emojibaseStore.allEmojis
                        .asSequence()
                        .filter { emoji ->
                            emoji.tags.orEmpty().any { it.contains(lowercaseQuery) } ||
                                emoji.shortcodes.any { it.contains(lowercaseQuery) }
                        }
                        .take(60)
                        .toImmutableList()
                }

                SearchBarResultState.Results(results)
            }
        }

        val isInPreview = LocalInspectionMode.current
        fun handleEvents(event: EmojiPickerEvents) {
            when (event) {
                // For some reason, in preview mode the SearchBar emits this event with an `isActive = true` value automatically
                is EmojiPickerEvents.ToggleSearchActive -> if (!isInPreview) {
                    isSearchActive = event.isActive
                }
                is EmojiPickerEvents.UpdateSearchQuery -> searchQuery = event.query
            }
        }

        return EmojiPickerState(
            categories = categories,
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            searchResults = emojiResults,
            eventSink = ::handleEvents,
        )
    }
}
