/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class EmojiPickerPresenter(
    private val emojibaseStore: EmojibaseStore,
    private val recentEmojis: ImmutableList<String>,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<EmojiPickerState> {
    @Composable
    override fun present(): EmojiPickerState {
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var emojiResults by remember { mutableStateOf<SearchBarResultState<ImmutableList<Emoji>>>(SearchBarResultState.Initial()) }

        val recentEmojiIcon = CompoundIcons.History()
        val categories = remember {
            val providedCategories = emojibaseStore.categories.map { (category, emojis) ->
                EmojiCategory(
                    titleId = category.title,
                    icon = IconSource.Vector(category.icon),
                    emojis = emojis
                )
            }
            if (recentEmojis.isNotEmpty()) {
                val recentEmojis = recentEmojis.mapNotNull { recentEmoji ->
                    emojibaseStore.allEmojis.find { it.unicode == recentEmoji }
                }.toImmutableList()
                val recentCategory =
                    EmojiCategory(
                        titleId = R.string.emoji_picker_category_recent,
                        icon = IconSource.Vector(recentEmojiIcon),
                        emojis = recentEmojis
                    )
                (listOf(recentCategory) + providedCategories).toImmutableList()
            } else {
                providedCategories.toImmutableList()
            }
        }

        LaunchedEffect(searchQuery) {
            emojiResults = if (searchQuery.isEmpty()) {
                SearchBarResultState.Initial()
            } else {
                // Add a small delay to avoid doing too many computations when the user is typing quickly
                delay(100.milliseconds)

                val lowercaseQuery = searchQuery.lowercase()
                val results = withContext(coroutineDispatchers.computation) {
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
        fun handleEvent(event: EmojiPickerEvents) {
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
            allEmojis = emojibaseStore.allEmojis,
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            searchResults = emojiResults,
            eventSink = ::handleEvent,
        )
    }
}
