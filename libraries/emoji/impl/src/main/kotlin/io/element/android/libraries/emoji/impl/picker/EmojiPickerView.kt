/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl.picker

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.emoji.impl.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun EmojiPickerView(
    state: DefaultEmojiPickerState,
    onSelectEmoji: (Emoji) -> Unit,
    selectedEmojis: ImmutableSet<String>,
    modifier: Modifier = Modifier,
    onBackspace: (() -> Unit)? = null,
    contentDescription: @Composable (emoji: Emoji, isSelected: Boolean) -> String = { emoji, _ -> emoji.unicode },
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.categories.size })
    var skinPickerEmoji by remember { mutableStateOf<Emoji?>(null) }

    Column(modifier) {
        SearchBar(
            modifier = Modifier.padding(bottom = if (state.isSearchActive) 0.dp else 10.dp),
            queryState = state.searchQuery,
            resultState = state.searchResults,
            active = state.isSearchActive,
            onActiveChange = { state.eventSink(EmojiPickerEvent.ToggleSearchActive(it)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            placeHolderTitle = stringResource(R.string.emoji_picker_search_placeholder),
        ) { emojis ->
            EmojiResults(
                emojis = emojis,
                isEmojiSelected = { selectedEmojis.contains(it.unicode) },
                onSelectEmoji = onSelectEmoji,
                onLongPress = { skinPickerEmoji = it },
                skinPickerEmoji = skinPickerEmoji,
                onDismissSkinPicker = { skinPickerEmoji = null },
                selectedEmojis = selectedEmojis,
                contentDescription = contentDescription,
            )
        }

        if (!state.isSearchActive) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                state.categories.forEachIndexed { index, category ->
                    Tab(
                        icon = {
                            when (category.icon) {
                                is IconSource.Resource -> Icon(
                                    resourceId = category.icon.id,
                                    contentDescription = stringResource(id = category.titleId)
                                )
                                is IconSource.Vector -> Icon(
                                    imageVector = category.icon.vector,
                                    contentDescription = stringResource(id = category.titleId)
                                )
                            }
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
                if (onBackspace != null) {
                    BackspaceTab(onBackspace = onBackspace)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { index ->
                val emojis = state.categories[index].emojis
                EmojiResults(
                    emojis = emojis,
                    isEmojiSelected = { selectedEmojis.contains(it.unicode) },
                    onSelectEmoji = onSelectEmoji,
                    onLongPress = { skinPickerEmoji = it },
                    skinPickerEmoji = skinPickerEmoji,
                    onDismissSkinPicker = { skinPickerEmoji = null },
                    selectedEmojis = selectedEmojis,
                    contentDescription = contentDescription,
                )
            }
        }
    }
}

/** Height of the icon-only tabs in the category row, beside which the backspace key is placed. */
private val BackspaceTabHeight = 48.dp
private val BackspaceRepeatInitialDelay = 400.milliseconds
private val BackspaceRepeatInterval = 50.milliseconds

/**
 * Backspace key at the end of the category row when the picker replaces the system keyboard, per the design;
 * long-press repeats deletion identically to a keyboard backspace.
 */
@Composable
private fun BackspaceTab(
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnBackspace by rememberUpdatedState(onBackspace)
    val interactionSource = remember { MutableInteractionSource() }
    val description = stringResource(R.string.emoji_picker_a11y_backspace)
    Box(
        modifier = modifier
            .height(BackspaceTabHeight)
            .semantics {
                role = Role.Button
                contentDescription = description
                onClick {
                    currentOnBackspace()
                    true
                }
            }
            .indication(interactionSource, ripple())
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        coroutineScope {
                            val repeater = launch {
                                currentOnBackspace()
                                delay(BackspaceRepeatInitialDelay)
                                while (isActive) {
                                    currentOnBackspace()
                                    delay(BackspaceRepeatInterval)
                                }
                            }
                            val released = tryAwaitRelease()
                            repeater.cancel()
                            interactionSource.emit(if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = CompoundIcons.BackspaceSolid(),
            contentDescription = null,
        )
    }
}

@Composable
private fun EmojiResults(
    emojis: ImmutableList<Emoji>,
    isEmojiSelected: (Emoji) -> Boolean,
    onSelectEmoji: (Emoji) -> Unit,
    onLongPress: (Emoji) -> Unit,
    skinPickerEmoji: Emoji?,
    onDismissSkinPicker: () -> Unit,
    selectedEmojis: ImmutableSet<String>,
    contentDescription: @Composable (emoji: Emoji, isSelected: Boolean) -> String,
) {
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
                isSelected = isEmojiSelected(item),
                onSelectEmoji = onSelectEmoji,
                onLongPress = onLongPress,
                skinPickerEmoji = skinPickerEmoji,
                onDismissSkinPicker = onDismissSkinPicker,
                emojiSize = 32.dp.toSp(),
                selectedSkinUnicodes = selectedEmojis,
                hasSelectedSkin = item.skins?.any { skin -> skin.unicode in selectedEmojis } == true,
                contentDescription = contentDescription,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerViewPreview(@PreviewParameter(DefaultEmojiPickerStatePreviewParam::class) state: DefaultEmojiPickerState) = ElementPreview {
    EmojiPickerView(
        state = state,
        onSelectEmoji = {},
        selectedEmojis = persistentSetOf("😀", "😄", "😃"),
        modifier = Modifier.fillMaxWidth(),
    )
}

@PreviewsDayNight
@Composable
internal fun EmojiPickerViewWithBackspacePreview(@PreviewParameter(
    DefaultEmojiPickerStatePreviewParam::class
) state: DefaultEmojiPickerState) = ElementPreview {
    EmojiPickerView(
        state = state,
        onSelectEmoji = {},
        selectedEmojis = persistentSetOf(),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        onBackspace = {},
    )
}
