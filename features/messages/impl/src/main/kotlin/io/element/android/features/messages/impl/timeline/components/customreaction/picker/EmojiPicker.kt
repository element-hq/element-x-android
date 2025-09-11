/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojiItem
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onSelectEmoji: (Emoji) -> Unit,
    state: EmojiPickerState,
    selectedEmojis: ImmutableSet<String>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val categories = state.categories
    val pagerState = rememberPagerState(pageCount = { EmojibaseCategory.entries.size })

    Column(modifier) {
        SearchBar(
            modifier = Modifier.padding(bottom = 10.dp),
            query = state.searchQuery,
            onQueryChange = { state.eventSink(EmojiPickerEvents.UpdateSearchQuery(it)) },
            resultState = state.searchResults,
            active = state.isSearchActive,
            onActiveChange = { state.eventSink(EmojiPickerEvents.ToggleSearchActive(it)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            placeHolderTitle = stringResource(CommonStrings.emoji_picker_search_placeholder),
        ) { results ->
            val emojis = results
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(emojis, key = { it.unicode }) { item ->
                    SelectableEmojiItem(
                        item = item,
                        selectedEmojis = selectedEmojis,
                        onSelectEmoji = onSelectEmoji,
                    )
                }
            }
        }

        if (!state.isSearchActive) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                EmojibaseCategory.entries.forEachIndexed { index, category ->
                    Tab(
                        icon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = stringResource(id = category.title)
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { index ->
                val category = EmojibaseCategory.entries[index]
                val emojis = categories[category] ?: listOf()
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(emojis, key = { it.unicode }) { item ->
                        SelectableEmojiItem(
                            item = item,
                            selectedEmojis = selectedEmojis,
                            onSelectEmoji = onSelectEmoji,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableEmojiItem(
    item: Emoji,
    selectedEmojis: ImmutableSet<String>,
    onSelectEmoji: (Emoji) -> Unit,
) {
    EmojiItem(
        modifier = Modifier.aspectRatio(1f),
        item = item,
        isSelected = selectedEmojis.contains(item.unicode),
        onSelectEmoji = onSelectEmoji,
        emojiSize = 32.dp.toSp(),
    )
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerPreview(@PreviewParameter(EmojiPickerStateProvider::class) state: EmojiPickerState) = ElementPreview {
    EmojiPicker(
        onSelectEmoji = {},
        state = state,
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        modifier = Modifier.fillMaxWidth(),
    )
}
