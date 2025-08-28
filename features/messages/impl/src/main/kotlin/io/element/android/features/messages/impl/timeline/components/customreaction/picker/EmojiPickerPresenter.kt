/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.emojibasebindings.allEmojis
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class EmojiPickerPresenter(
    private val emojibaseStore: EmojibaseStore,
    private val dispatchers: CoroutineDispatchers = CoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    ),
) : Presenter<EmojiPickerState> {
    private var emojiSearchJob: Job? = null

    @Composable
    override fun present(): EmojiPickerState {
        val coroutineScope = rememberCoroutineScope()

        val categories = remember { emojibaseStore.categories.toPersistentMap() }
        var searchQuery by remember { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<SearchBarResultState<ImmutableList<Emoji>>>(SearchBarResultState.Initial()) }

        fun handleEvent(event: EmojiPickerEvents) {
            when (event) {
                is EmojiPickerEvents.UpdateSearchQuery -> {
                    searchQuery = event.query
                    emojiSearchJob?.cancel()

                    emojiSearchJob = coroutineScope.launch(dispatchers.computation) {
                        delay(250.milliseconds)
                        searchResults = if (searchQuery.isEmpty()) {
                            SearchBarResultState.Initial()
                        } else {
                            val results = emojibaseStore.allEmojis.filter { emoji ->
                                emoji.label.contains(event.query) || emoji.shortcodes.any { it.contains(event.query) }
                            }
                            if (results.isEmpty()) {
                                SearchBarResultState.NoResultsFound()
                            } else {
                                SearchBarResultState.Results(results.toImmutableList())
                            }
                        }
                    }
                }
            }
        }

        return EmojiPickerState(
            categories = categories,
            searchQuery = searchQuery,
            searchResults = searchResults,
            eventSink = ::handleEvent,
        )
    }
}

data class EmojiPickerState(
    val categories: ImmutableMap<EmojibaseCategory, List<Emoji>>,
    val searchQuery: String,
    val searchResults: SearchBarResultState<ImmutableList<Emoji>>,
    val eventSink: (EmojiPickerEvents) -> Unit,
)

sealed interface EmojiPickerEvents {
    data class UpdateSearchQuery(val query: String) : EmojiPickerEvents
}
