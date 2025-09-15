/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.features.messages.impl.timeline.components.customreaction.DefaultEmojibaseProvider
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojiItem
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onSelectEmoji: (Emoji) -> Unit,
    state: EmojiPickerState,
    selectedEmojis: ImmutableSet<String>,
    recentEmojis: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val categories = remember { state.categories }
    val hasRecentEmojis = recentEmojis.isNotEmpty()
    val pagerState = rememberPagerState(pageCount = {
        if (hasRecentEmojis) {
            EmojibaseCategory.entries.size + 1
        } else {
            EmojibaseCategory.entries.size
        }
    })
    Column(modifier) {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            if (hasRecentEmojis) {
                Tab(
                    icon = {
                        Icon(
                            // TODO: use a proper icon and string
                            imageVector = Icons.Default.History,
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
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val emojis = if (hasRecentEmojis && index == 0) {
                    recentEmojis.mapNotNull { recent -> state.allEmojis.find { it.unicode == recent } }.toPersistentList()
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

@PreviewsDayNight
@Composable
internal fun EmojiPickerPreview() = ElementPreview {
    val emojibase = DefaultEmojibaseProvider(LocalContext.current).emojibaseStore
    EmojiPicker(
        recentEmojis = persistentListOf("😀", "😄", "😃", "🌴"),
        onSelectEmoji = {},
        state = EmojiPickerState(
            categories = emojibase.categories,
            allEmojis = emojibase.allEmojis,
            searchQuery = "",
            isSearchActive = false,
            searchResults = SearchBarResultState.Initial(),
            eventSink = {},
        ),
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        modifier = Modifier.fillMaxWidth(),
    )
}
