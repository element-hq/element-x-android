/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl.picker

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.emoji.api.picker.EmojiPickerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class DefaultEmojiPickerStatePreviewParam : PreviewParameterProvider<DefaultEmojiPickerState> {
    override val values: Sequence<DefaultEmojiPickerState>
        get() = sequenceOf(
            aDefaultEmojiPickerState(),
            aDefaultEmojiPickerState(isSearchActive = true),
            aDefaultEmojiPickerState(isSearchActive = true, searchQuery = "smile"),
            aDefaultEmojiPickerState(
                isSearchActive = true,
                searchQuery = "smile",
                searchResults = SearchBarResultState.Results(emojiList())
            ),
        )
}

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
        "🥲",
        null
    )
)

internal fun aDefaultEmojiPickerState(
    categories: ImmutableList<EmojiCategory> = EmojibaseCategory.entries.map {
        EmojiCategory(
            titleId = it.title,
            icon = IconSource.Vector(it.icon),
            emojis = emojiList(),
        )
    }.toImmutableList(),
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<ImmutableList<Emoji>> = SearchBarResultState.Initial,
    eventSink: (EmojiPickerEvent) -> Unit = {},
) = DefaultEmojiPickerState(
    categories = categories,
    searchQuery = TextFieldState(initialText = searchQuery),
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    eventSink = eventSink,
)

/**
 * Public helper for external tests that need an [EmojiPickerState] instance the impl's
 * [DefaultEmojiPickerRenderer] will accept. The no-arg overload returns a ready state
 * (see [EmojiPickerState.isReady]); use the [emojis] overload to inject specific categories.
 */
fun anEmojiPickerState(): EmojiPickerState = aDefaultEmojiPickerState()

fun anEmojiPickerState(
    emojis: Map<EmojibaseCategory, ImmutableList<Emoji>>,
): EmojiPickerState = aDefaultEmojiPickerState(
    categories = emojis.map { (category, list) ->
        EmojiCategory(
            titleId = category.title,
            icon = IconSource.Vector(category.icon),
            emojis = list,
        )
    }.toImmutableList(),
)
