/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.api.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import io.element.android.emojibasebindings.Emoji
import kotlinx.collections.immutable.ImmutableSet

@Immutable
interface EmojiPickerRenderer {
    /**
     * @param state opaque state produced by [EmojiPickerPresenter.present].
     * @param onSelectEmoji invoked when the user taps an emoji (including a skin-tone variant).
     * @param selectedEmojis emojis to visually mark as already selected.
     * @param modifier layout modifier for the picker container.
     * @param contentDescription accessibility label for each emoji cell.
     */
    @Composable
    fun Render(
        state: EmojiPickerState,
        onSelectEmoji: (Emoji) -> Unit,
        selectedEmojis: ImmutableSet<String>,
        modifier: Modifier = Modifier,
        contentDescription: @Composable (Emoji, Boolean) -> String = { emoji, _ -> emoji.unicode },
    )
}

object NoOpEmojiPickerRenderer : EmojiPickerRenderer {
    @Composable
    override fun Render(
        state: EmojiPickerState,
        onSelectEmoji: (Emoji) -> Unit,
        selectedEmojis: ImmutableSet<String>,
        modifier: Modifier,
        contentDescription: @Composable (Emoji, Boolean) -> String,
    ) = Unit
}
