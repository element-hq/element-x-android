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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.Emoji
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.customreaction.DefaultEmojibaseProvider
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojiItem
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import okhttp3.internal.immutableListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onSelectEmoji: (Emoji) -> Unit,
    state: EmojiPickerState,
    selectedEmojis: ImmutableSet<String>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.categories.size })
    Column(modifier) {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            state.categories.forEachIndexed { index, category ->
                Tab(
                    icon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = stringResource(id = category.titleId)
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
                val emojis = state.categories[index].emojis
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

    // Simulate recent emojis with the first 10 emojis we can fetch
    val recentsCategory = EmojiCategory(
        titleId = R.string.emoji_picker_category_recent,
        icon = CompoundIcons.History(),
        emojis = emojibase.allEmojis.take(10).toPersistentList(),
    )

    val providedCategories = emojibase.categories.map { (category, emojis) ->
        EmojiCategory(
            titleId = category.title,
            icon = category.icon,
            emojis = emojis.toPersistentList(),
        )
    }.toPersistentList()

    EmojiPicker(
        onSelectEmoji = {},
        state = EmojiPickerState(
            categories = (immutableListOf(recentsCategory) + providedCategories).toImmutableList(),
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
