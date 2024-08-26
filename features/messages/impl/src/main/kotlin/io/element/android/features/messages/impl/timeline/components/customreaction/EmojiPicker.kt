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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojiSkin
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onSelectEmoji: (String) -> Unit,
    emojibaseStore: EmojibaseStore,
    selectedEmojis: ImmutableSet<String>,
    modifier: Modifier = Modifier,
    skinTone: String? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val categories = remember { emojibaseStore.categories }
    val pagerState = rememberPagerState(pageCount = { EmojibaseCategory.entries.size })
    Column(modifier) {
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

            val emojiSize = 32.dp.toSp()
            val contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)

            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(emojis, key = { it.unicode }) { item ->
                    val content = @Composable {
                        val emoji = if (skinTone != null) item.withSkinTone(skinTone)?.unicode ?: item.unicode else item.unicode
                        EmojiItem(
                            modifier = Modifier.aspectRatio(1f),
                            emoji = emoji,
                            emojiSize = emojiSize,
                            isSelected = selectedEmojis.contains(emoji),
                            onSelectEmoji = onSelectEmoji,
                            onLongPress = {},
                        )
                    }

                    if (item.skins != null) {
                        val variants = listOf(item.unicode) + item.skins!!.map(EmojiSkin::unicode)
                        EmojiPickerTooltip(
                            emojis = variants,
                            emojiSize = emojiSize,
                            contentPadding = contentPadding,
                            arrangement = Arrangement.spacedBy(16.dp),
                            selectedEmojis = selectedEmojis,
                            onSelectEmoji = onSelectEmoji,
                            content = content,
                        )
                    } else content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerTooltip(
    emojis: List<String>,
    contentPadding: PaddingValues,
    arrangement: Arrangement.HorizontalOrVertical,
    selectedEmojis: ImmutableSet<String>,
    onSelectEmoji: (String) -> Unit,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 20.sp,
    tooltipVisible: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val tooltipState = rememberTooltipState(initialIsVisible = tooltipVisible, isPersistent = true)

    // Note that this renders wrongly (with a spurious background box) in
    // the preview, but it's fine when running the app for realsies
    // https://issuetracker.google.com/issues/308808808
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ElementTheme.colors.bgSubtleSecondary)
                    .border(1.dp, ElementTheme.colors.borderInteractiveSecondary, CircleShape)
                    .horizontalScroll(scrollState),
            ) {
                Row(
                    modifier = modifier.padding(contentPadding),
                    horizontalArrangement = arrangement,
                ) {
                    emojis.forEach { variant ->
                        EmojiItem(
                            emoji = variant,
                            emojiSize = emojiSize,
                            isSelected = selectedEmojis.contains(variant),
                            onSelectEmoji = {
                                scope.launch { tooltipState.dismiss() }
                                onSelectEmoji(it)
                            },
                            onLongPress = {}
                        )
                    }
                }
            }
        },
        state = tooltipState,
        content = content,
    )
}

private fun Emoji.withSkinTone(tone: String): EmojiSkin? {
    if (tone !in SKIN_MODIFIERS) return null
    return skins?.firstOrNull { skin -> tone in skin.unicode }
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerPreview() = ElementPreview {
    EmojiPicker(
        onSelectEmoji = {},
        emojibaseStore = EmojibaseDatasource().load(LocalContext.current),
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        modifier = Modifier.fillMaxWidth(),
    )
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerTooltipPreview() {
    val emojiSize = 32.dp.toSp()
    val contentPadding = PaddingValues(14.dp)
    val arrangement = Arrangement.spacedBy(12.dp)

    ElementPreview {
        Box(
            modifier = Modifier.size(500.dp, 300.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmojiPickerTooltip(
                modifier = Modifier.padding(4.dp),
                emojis = (setOf("") + SKIN_MODIFIERS).map { "🤌$it" },
                emojiSize = emojiSize,
                contentPadding = contentPadding,
                arrangement = arrangement,
                selectedEmojis = persistentSetOf("🤌🏽"),
                onSelectEmoji = {},
                tooltipVisible = true,
            ) {
                EmojiItem(
                    emoji = "🤌",
                    emojiSize = emojiSize,
                    isSelected = true,
                    onSelectEmoji = {},
                    onLongPress = {},
                )
            }
        }
    }
}

