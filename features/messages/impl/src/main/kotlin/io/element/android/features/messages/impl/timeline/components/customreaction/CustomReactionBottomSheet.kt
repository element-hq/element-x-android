/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.element.android.emojibasebindings.Emoji
import io.element.android.features.messages.impl.timeline.a11y.a11yReactionAction
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.emoji.api.picker.EmojiPickerRenderer
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onSelectEmoji: (EventOrTransactionId, Emoji) -> Unit,
    emojiPickerRenderer: EmojiPickerRenderer,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    when (state.target) {
        CustomReactionState.Target.None,
        is CustomReactionState.Target.Loading -> Unit
        is CustomReactionState.Target.Success -> {
            fun onEmojiSelectedDismiss(emoji: Emoji) {
                sheetState.hide(coroutineScope) {
                    state.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
                    onSelectEmoji(state.target.event.eventOrTransactionId, emoji)
                }
            }

            fun onDismiss() {
                state.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
            }

            ModalBottomSheet(
                onDismissRequest = ::onDismiss,
                sheetState = sheetState,
                modifier = modifier,
                scrollable = false,
            ) {
                ExpandSheetWhenImeIsVisible(sheetState)
                emojiPickerRenderer.Render(
                    state = state.target.emojiPickerState,
                    onSelectEmoji = ::onEmojiSelectedDismiss,
                    selectedEmojis = state.selectedEmoji,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = { emoji, isSelected ->
                        a11yReactionAction(emoji = emoji.unicode, userAlreadyReacted = isSelected)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandSheetWhenImeIsVisible(sheetState: SheetState) {
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            sheetState.expand()
        }
    }
}
