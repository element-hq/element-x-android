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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.emojibasebindings.allEmojis
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojiItem
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.SearchBar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onSelectEmoji: (Emoji) -> Unit,
    emojibaseStore: EmojibaseStore,
    selectedEmojis: ImmutableSet<String>,
    recentlyUsedEmojis: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val presenter = remember { EmojiPickerPresenter(emojibaseStore) }
    val state = presenter.present()
    val coroutineScope = rememberCoroutineScope()
    val categories = state.categories
    val pagerState = rememberPagerState(pageCount = {
        if (recentlyUsedEmojis.isEmpty()) {
            EmojibaseCategory.entries.size
        } else {
            EmojibaseCategory.entries.size + 1
        }
    })
    Column(modifier) {
        var searchBarActive by remember { mutableStateOf(false) }
        SearchBar(
            modifier = Modifier.padding(bottom = 10.dp),
            query = state.searchQuery,
            onQueryChange = { state.eventSink(EmojiPickerEvents.UpdateSearchQuery(it)) },
            resultState = state.searchResults,
            active = searchBarActive,
            onActiveChange = { searchBarActive = it },
            windowInsets = WindowInsets(0, 0, 0, 0),
            // TODO: use a proper string
            placeHolderTitle = "Search emojis",
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
                    EmojiItem(
                        modifier = Modifier.aspectRatio(1f),
                        item = item,
                        isSelected = selectedEmojis.contains(item.unicode),
                        onSelectEmoji = onSelectEmoji,
                        emojiSize = 32.dp.toSp(),
                    )
                }
            }
        }

        val hasRecentEmojis = recentlyUsedEmojis.isNotEmpty()
        if (!searchBarActive) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                if (hasRecentEmojis) {
                    Tab(
                        icon = {
                            Icon(
                                // TODO: use a proper icon and string
                                imageVector = CompoundIcons.Image(),
                                contentDescription = "Recent",
                            )
                        },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )
                }
                EmojibaseCategory.entries.forEachIndexed { index, category ->
                    val actualIndex = if (hasRecentEmojis) index + 1 else index
                    Tab(
                        icon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = stringResource(id = category.title)
                            )
                        },
                        selected = pagerState.currentPage == actualIndex,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(actualIndex) }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { index ->
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val emojis = if (hasRecentEmojis && index == 0) {
                        val allEmojis = emojibaseStore.allEmojis
                        recentlyUsedEmojis.mapNotNull { recent -> allEmojis.find { it.unicode == recent } }.toPersistentList()
                    } else {
                        val actualIndex = if (hasRecentEmojis) index - 1 else index
                        val category = EmojibaseCategory.entries[actualIndex]
                        categories[category] ?: listOf()
                    }
                    items(emojis, key = { it.unicode }) { item ->
                        EmojiItem(
                            modifier = Modifier.aspectRatio(1f),
                            item = item,
                            isSelected = selectedEmojis.contains(item.unicode),
                            onSelectEmoji = onSelectEmoji,
                            emojiSize = 32.dp.toSp(),
                        )
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerPreview() = ElementPreview {
    EmojiPicker(
        onSelectEmoji = {},
        emojibaseStore = EmojibaseDatasource().load(LocalContext.current),
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        recentlyUsedEmojis = persistentListOf(),
        modifier = Modifier.fillMaxWidth(),
    )
}
