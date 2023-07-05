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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanniktech.emoji.Emoji
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiPicker(
    onEmojiSelected: (Emoji) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val emojiProvider = remember { GoogleEmojiProvider() }
    val categories = remember { emojiProvider.categories }
    val pagerState = rememberPagerState()
    Column (modifier) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    text = {
                        Icon(
                            resourceId = emojiProvider.getIcon(category),
                            contentDescription = category.categoryNames["en"]
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
            pageCount = categories.size,
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { index ->
            val category = categories[index]
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 40.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                items(category.emojis, key = { it.unicode }) { item ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                enabled = true,
                                onClick = { onEmojiSelected(item) },
                                indication = rememberRipple(bounded = false, radius = 20.dp),
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.unicode, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun EmojiPickerLightPreview() {
    ElementPreviewLight { ContentToPreview() }
}

@Preview
@Composable
internal fun EmojiPickerDarkPreview() {
    ElementPreviewDark { ContentToPreview() }
}

@Composable
private fun ContentToPreview() {
    EmojiPicker(
        onEmojiSelected = {},
        modifier = Modifier.fillMaxWidth()
    )
}
