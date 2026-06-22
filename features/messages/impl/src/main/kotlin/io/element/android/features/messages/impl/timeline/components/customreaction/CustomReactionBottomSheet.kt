/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.Emoji
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.EmojiPicker
import io.element.android.features.messages.impl.timeline.components.customreaction.picker.EmojiPickerPresenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onSelectEmoji: (EventOrTransactionId, Emoji) -> Unit,
    onSelectCustomReaction: (EventOrTransactionId, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val target = state.target as? CustomReactionState.Target.Success

    fun onDismiss() {
        state.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
    }

    fun onEmojiSelectedDismiss(emoji: Emoji) {
        if (target?.event == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
            onSelectEmoji(target.event.eventOrTransactionId, emoji)
        }
    }

    fun onTextReactionSelectedDismiss(text: String) {
        if (target?.event == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
            onSelectCustomReaction(target.event.eventOrTransactionId, text)
        }
    }

    if (target?.emojibaseStore != null && target.event.eventId != null) {
        ModalBottomSheet(
            onDismissRequest = ::onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            scrollable = false,
        ) {
            // Text reaction input
            var textReaction by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = textReaction,
                    onValueChange = { textReaction = it.take(64) },
                    modifier = Modifier.weight(1f),
                    placeholder = "Custom reaction...",
                    singleLine = true,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textReaction.isNotBlank()) {
                            val reaction = textReaction.trim()
                            textReaction = ""
                            onTextReactionSelectedDismiss(reaction)
                        }
                    },
                    enabled = textReaction.isNotBlank(),
                ) {
                    Icon(contentDescription = "Send reaction", imageVector = CompoundIcons.Send())
                }
            }

            val presenter = remember {
                EmojiPickerPresenter(
                    emojibaseStore = target.emojibaseStore,
                    recentEmojis = state.recentEmojis,
                    coroutineDispatchers = CoroutineDispatchers.Default,
                )
            }
            EmojiPicker(
                onSelectEmoji = ::onEmojiSelectedDismiss,
                state = presenter.present(),
                selectedEmojis = state.selectedEmoji,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
