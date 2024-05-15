/*
 * Copyright (c) 2023 New Vector Ltd
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.emojibasebindings.allEmojis
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.TextField
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onEmojiSelected: (Emoji) -> Unit,
    emojibaseStore: EmojibaseStore,
    selectedEmojis: ImmutableSet<String>,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val categories = remember { emojibaseStore.categories }
    val pagerState = rememberPagerState(pageCount = { EmojibaseCategory.entries.size })
    val allEmojis = remember { emojibaseStore.allEmojis }
    Column(modifier) {
        val showCategories = remember { MutableTransitionState(true) }
        val filteredEmojis by produceState(initialValue = persistentListOf<Emoji>(), searchQuery) {
            if (searchQuery.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    val lowercaseQuery = searchQuery.lowercase()
                    value = allEmojis.filter { emoji ->
                        emoji.shortcodes.any { it.contains(lowercaseQuery, ignoreCase = false) }
                    }.toPersistentList()
                }
            } else {
                value = persistentListOf()
            }
        }
        LaunchedEffect(searchQuery) {
            showCategories.targetState = searchQuery.isEmpty()
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = { searchQuery = it }
        )

        AnimatedVisibility(visibleState = showCategories) {
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
        }

        if (showCategories.targetState) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { index ->
                val category = EmojibaseCategory.entries[index]
                val emojis = remember(category) { categories[category] ?: listOf() }
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
                            onEmojiSelected = onEmojiSelected,
                            emojiSize = 32.dp.toSp(),
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filteredEmojis, key = { it.unicode }) { item ->
                    EmojiItem(
                        modifier = Modifier.aspectRatio(1f),
                        item = item,
                        isSelected = selectedEmojis.contains(item.unicode),
                        onEmojiSelected = onEmojiSelected,
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
    EmojiPicker(
        onEmojiSelected = {},
        emojibaseStore = EmojibaseDatasource().load(LocalContext.current),
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        modifier = Modifier.fillMaxWidth(),
    )
}
