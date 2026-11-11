/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
 * current orientation, i.e. until the keyboard has appeared once. It shrinks on small screens to keep
 * the composer visible. Once the keyboard height is known, the picker occupies exactly that space.
 */
private val ComposerEmojiPickerMaxHeight = 300.dp

/**
 * Smallest keyboard space the picker will occupy. Below that threshold, e.g. with a floating keyboard,
 * the picker falls back to [ComposerEmojiPickerMaxHeight].
 */
private val ComposerEmojiPickerMinHeight = 160.dp

/**
 * Height in pixels of the emoji picker, or 0 while it is hidden. The picker claims exactly the space of the last soft keyboard
 * seen in this orientation, so the composer stays put when one replaces the other. Until a keyboard has been seen, it takes
 * half of [availableHeightPx], capped at [ComposerEmojiPickerMaxHeight].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun rememberComposerEmojiPickerHeightPx(isShown: Boolean, availableHeightPx: Int): Int {
    val density = LocalDensity.current
    val orientation = LocalConfiguration.current.orientation
    // Navigation-bar area included, as reported by the IME insets.
    var keyboardHeightPx by rememberSaveable(orientation) { mutableIntStateOf(0) }
    val imeTargetBottomPx = WindowInsets.imeAnimationTarget.getBottom(density)
    LaunchedEffect(imeTargetBottomPx) {
        if (imeTargetBottomPx > 0) {
            keyboardHeightPx = imeTargetBottomPx
        }
    }
    val systemBarsBottomPx = WindowInsets.systemBars.getBottom(density)
    if (!isShown) return 0
    val keyboardSpacePx = keyboardHeightPx - systemBarsBottomPx
    return if (keyboardSpacePx >= with(density) { ComposerEmojiPickerMinHeight.roundToPx() }) {
        keyboardSpacePx
    } else {
        (availableHeightPx / 2).coerceAtMost(with(density) { ComposerEmojiPickerMaxHeight.roundToPx() })
    }
}

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

/**
 * The `composerEmojiPicker` slot of the messages screens, shared by the room and thread timelines.
 */
internal fun composerEmojiPickerContent(
    state: MessageComposerState,
    emojiPickerRenderer: EmojiPickerRenderer,
): @Composable (height: Dp) -> Unit = { height ->
    ComposerEmojiPicker(
        state = state,
        emojiPickerRenderer = emojiPickerRenderer,
        height = height,
    )
}
