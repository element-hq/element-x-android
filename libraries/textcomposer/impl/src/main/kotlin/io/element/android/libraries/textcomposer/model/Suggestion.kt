/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
