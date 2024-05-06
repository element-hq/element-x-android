/*
 * Copyright (c) 2024 New Vector Ltd
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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.element.android.libraries.textcomposer.components.ImmutableCharSequence
import io.element.android.wysiwyg.compose.RichTextEditorState

sealed interface TextEditorState {
    data class Markdown(
        val state: MarkdownTextEditorState,
    ) : TextEditorState

    data class Rich(
        val richTextEditorState: RichTextEditorState
    ) : TextEditorState

    fun hasFocus(): Boolean = when (this) {
        is Markdown -> state.hasFocus
        is Rich -> richTextEditorState.hasFocus
    }

    suspend fun reset() {
        when (this) {
            is Markdown -> {
                state.selection = IntRange.EMPTY
                state.text.update("", true)
            }
            is Rich -> richTextEditorState.setHtml("")
        }
    }

    suspend fun requestFocus() {
        when (this) {
            is Markdown -> state.onRequestFocus()
            is Rich -> richTextEditorState.requestFocus()
        }
    }

    val lineCount: Int get() = when (this) {
        is Markdown -> state.lineCount
        is Rich -> richTextEditorState.lineCount
    }
}

@Stable
class MarkdownTextEditorState(
    initialText: String? = null,
) {
    var text by mutableStateOf(ImmutableCharSequence(initialText ?: ""))
    var selection by mutableStateOf(0..0)
    var hasFocus by mutableStateOf(false)
    var onRequestFocus by mutableStateOf({})
    var lineCount by mutableIntStateOf(1)
    var currentMentionSuggestion by mutableStateOf<Suggestion?>(null)
}
