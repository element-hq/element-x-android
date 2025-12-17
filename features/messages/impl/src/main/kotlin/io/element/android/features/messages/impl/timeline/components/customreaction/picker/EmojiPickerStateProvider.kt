/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.components.customreaction.icon
import io.element.android.features.messages.impl.timeline.components.customreaction.title
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class EmojiPickerStateProvider : PreviewParameterProvider<EmojiPickerState> {
    override val values: Sequence<EmojiPickerState>
        get() = sequenceOf(
            anEmojiPickerState(),
            anEmojiPickerState(isSearchActive = true),
            anEmojiPickerState(isSearchActive = true, searchQuery = "smile"),
            anEmojiPickerState(
                isSearchActive = true,
                searchQuery = "smile",
                searchResults = SearchBarResultState.Results(emojiList())
            ),
        )
}

private fun recentEmojisCategory() = EmojiCategory(
    titleId = R.string.emoji_picker_category_recent,
    icon = IconSource.Resource(CompoundDrawables.ic_compound_history),
    emojis = emojiList(),
)

private fun emojiList(): ImmutableList<Emoji> = persistentListOf(
    Emoji(
        "0x00",
        "grinning face",
        persistentListOf("grinning"),
        persistentListOf("smile, grin"),
        "😀",
        null
    ),
    Emoji(
        "0x01",
        "crying face",
        persistentListOf("crying"),
        persistentListOf("smile, crying"),
        "\uD83E\uDD72",
        null
    )
)

internal fun anEmojiPickerState(
    categories: ImmutableList<EmojiCategory> = (listOf(recentEmojisCategory()) + EmojibaseCategory.entries.map {
        EmojiCategory(
            titleId = it.title,
            icon = IconSource.Vector(it.icon),
            emojis = emojiList(),
        )
    }).toImmutableList(),
    allEmojis: ImmutableList<Emoji> = categories.flatMap { it.emojis }.toImmutableList(),
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<ImmutableList<Emoji>> = SearchBarResultState.Initial(),
    eventSink: (EmojiPickerEvents) -> Unit = {},
) = EmojiPickerState(
    categories = categories,
    allEmojis = allEmojis,
    searchQuery = searchQuery,
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    eventSink = eventSink,
)
