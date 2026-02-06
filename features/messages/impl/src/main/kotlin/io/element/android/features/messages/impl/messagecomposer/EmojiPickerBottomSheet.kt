/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.EmojiPicker
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.EmojiPickerPresenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun EmojiPickerView(
    state: MessageComposerState,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    if (state.emojibaseStore != null) {
        EmojiPickerContent(
            state = state,
            height = height,
            modifier = modifier,
        )
    }
}

@Composable
private fun EmojiPickerContent(
    state: MessageComposerState,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val presenter = remember {
            state.emojibaseStore?.let {
                EmojiPickerPresenter(
                    emojibaseStore = it,
                    recentEmojis = state.recentEmojis,
                    coroutineDispatchers = CoroutineDispatchers.Default,
                )
            }
        }
        
        presenter?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(ElementTheme.colors.bgCanvasDefault)
            ) {
                EmojiPicker(
                    onSelectEmoji = { emoji ->
                        state.eventSink(MessageComposerEvent.InsertEmoji(emoji))
                    },
                    state = it.present(),
                    selectedEmojis = persistentSetOf(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
