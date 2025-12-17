/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList

// Emoji is unstable (because from an external library?), so we annotate with @Immutable
@Immutable
data class EmojiPickerState(
    val categories: ImmutableList<EmojiCategory>,
    val allEmojis: ImmutableList<Emoji>,
    val searchQuery: String,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<Emoji>>,
    val eventSink: (EmojiPickerEvents) -> Unit,
)

/**
 * Represents a category of emojis with a title id, icon, and the list of associated emojis.
 */
data class EmojiCategory(
    @StringRes val titleId: Int,
    val icon: IconSource,
    val emojis: ImmutableList<Emoji>,
)
