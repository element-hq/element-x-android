/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import uniffi.wysiwyg_composer.PatternKey
import uniffi.wysiwyg_composer.SuggestionPattern

data class Suggestion(
    val start: Int,
    val end: Int,
    val type: SuggestionType,
    val text: String,
) {
    constructor(suggestion: SuggestionPattern) : this(
        suggestion.start.toInt(),
        suggestion.end.toInt(),
        SuggestionType.fromPatternKey(suggestion.key),
        suggestion.text,
    )
}

sealed interface SuggestionType {
    data object Mention : SuggestionType
    data object Command : SuggestionType
    data object Room : SuggestionType
    data class Custom(val pattern: String) : SuggestionType

    companion object {
        fun fromPatternKey(key: PatternKey): SuggestionType {
            return when (key) {
                PatternKey.At -> Mention
                PatternKey.Slash -> Command
                PatternKey.Hash -> Room
                is PatternKey.Custom -> Custom(key.v1)
            }
        }
    }
}
