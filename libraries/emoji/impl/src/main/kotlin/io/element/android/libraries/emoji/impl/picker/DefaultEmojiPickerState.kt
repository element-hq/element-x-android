/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl.picker

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.emoji.api.picker.EmojiPickerState
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class DefaultEmojiPickerState(
    val categories: ImmutableList<EmojiCategory>,
    val searchQuery: TextFieldState,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<Emoji>>,
    val eventSink: (EmojiPickerEvent) -> Unit,
) : EmojiPickerState {
    override val isReady = categories.isNotEmpty()
}

internal data class EmojiCategory(
    @StringRes val titleId: Int,
    val icon: IconSource,
    val emojis: ImmutableList<Emoji>,
)
