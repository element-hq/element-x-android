/*
 * Copyright (c) 2024 New Vector Ltd
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.allEmojis
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class EmojiPickerStatePresenter @Inject constructor(
    private val emojibaseProvider: EmojibaseProvider,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<EmojiPickerState> {
    @Composable
    override fun present(): EmojiPickerState {
        val startActive by sessionPreferencesStore.isReactionPickerSearchEnabled().collectAsState(initial = false)
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchActive by rememberSaveable(startActive) { mutableStateOf(startActive) }
        val searchResults = remember { mutableStateOf<SearchBarResultState<List<Emoji>>>(SearchBarResultState.Initial()) }

        LaunchedEffect(searchQuery) {
            searchResults.value = searchEmojis(searchQuery, emojibaseProvider.emojibaseStore.allEmojis)
        }

        return EmojiPickerState(
            startActive = startActive,
            isSearchActive = searchActive,
            searchQuery = searchQuery,
            searchResults = searchResults.value,
            eventSink = {
                when (it) {
                    is EmojiPickerEvents.OnSearchActiveChanged -> {
                        searchActive = it.active
                    }
                    is EmojiPickerEvents.UpdateSearchQuery -> {
                        searchQuery = it.query
                    }
                    is EmojiPickerEvents.Reset -> {
                        searchActive = startActive
                        searchQuery = ""
                    }
                }
            }
        )
    }
}

fun searchEmojis(searchQuery: String, allEmojis: List<Emoji>): SearchBarResultState<List<Emoji>> {
    val query = searchQuery.trim()
    if (query == "")
        return SearchBarResultState.Initial()

    val matches = allEmojis.filter { emoji ->
        emoji.unicode == query
            || emoji.label.contains(query, true)
            || emoji.tags?.any { it.contains(query, true) }.orFalse()
            || emoji.shortcodes.any { it.contains(query, true) }
    }.toImmutableList()

    return if (matches.isEmpty()) SearchBarResultState.NoResultsFound() else SearchBarResultState.Results(matches)
}
