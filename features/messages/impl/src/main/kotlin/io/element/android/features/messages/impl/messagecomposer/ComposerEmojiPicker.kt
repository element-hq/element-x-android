/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.emoji.api.picker.EmojiPickerRenderer
import io.element.android.libraries.emoji.api.picker.NoOpEmojiPickerRenderer
import kotlinx.collections.immutable.persistentSetOf

/**
 * Height of the emoji picker shown beneath the composer while the soft-keyboard height remains unknown for the
 * current orientation, i.e. until the keyboard has appeared once. The caller shrinks it on small screens to keep
 * the composer visible. Once the keyboard height is known, the picker occupies exactly that space.
 */
internal val ComposerEmojiPickerMaxHeight = 300.dp

/**
 * Smallest keyboard space the picker will occupy. Below that threshold, e.g. with a floating keyboard,
 * the picker falls back to [ComposerEmojiPickerMaxHeight].
 */
internal val ComposerEmojiPickerMinHeight = 160.dp

/**
 * Emoji picker shown below the composer, replacing the soft keyboard. Tapping an emoji inserts it
 * into the composer at the cursor and leaves the picker open. The [height] must be fixed: the
 * composer container measures the intrinsic height of its content.
 */
@Composable
internal fun ComposerEmojiPicker(
    state: MessageComposerState,
    emojiPickerRenderer: EmojiPickerRenderer,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val sheetState = state.emojiPickerState
    if (sheetState == ComposerEmojiPickerState.Hidden) return
    BackHandler {
        state.eventSink(MessageComposerEvent.DismissEmojiPicker)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        when (sheetState) {
            is ComposerEmojiPickerState.Shown -> emojiPickerRenderer.Render(
                state = sheetState.state,
                onSelectEmoji = { emoji -> state.eventSink(MessageComposerEvent.InsertEmoji(emoji.unicode)) },
                selectedEmojis = persistentSetOf(),
                modifier = Modifier.fillMaxSize(),
                onBackspace = { state.eventSink(MessageComposerEvent.DeleteBackward) },
            )
            // Loading
            else -> CircularProgressIndicator()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ComposerEmojiPickerPreview() = ElementPreview {
    ComposerEmojiPicker(
        state = aMessageComposerState(emojiPickerState = ComposerEmojiPickerState.Loading),
        emojiPickerRenderer = NoOpEmojiPickerRenderer,
        height = ComposerEmojiPickerMaxHeight,
    )
}
