/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap

class EmojiPickerStateProvider : PreviewParameterProvider<EmojiPickerState> {
    override val values: Sequence<EmojiPickerState>
        get() = sequenceOf(
            anEmojiPickerState(),
            anEmojiPickerState(isSearchActive = true),
            anEmojiPickerState(isSearchActive = true, searchQuery = "smile"),
            anEmojiPickerState(
                isSearchActive = true,
                searchQuery = "smile",
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
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
                        ),
                    )
                )
            ),
        )
}

internal fun anEmojiPickerState(
    categories: ImmutableMap<EmojibaseCategory, ImmutableList<Emoji>> = EmojibaseCategory.entries.associateWith {
        persistentListOf(
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
            ),
        )
    }.toImmutableMap(),
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<ImmutableList<Emoji>> = SearchBarResultState.Initial(),
    eventSink: (EmojiPickerEvents) -> Unit = {},
) = EmojiPickerState(
    categories = categories,
    searchQuery = searchQuery,
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    eventSink = eventSink,
)
