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

package io.element.android.libraries.textcomposer.components

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.widget.EditText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.getSpans
import androidx.core.widget.addTextChangedListener
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun MarkdownTextInput(
    state: MarkdownTextEditorState,
    subcomposing: Boolean,
    onTyping: (Boolean) -> Unit,
    onSuggestionReceived: (Suggestion?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MarkdownEditText(context).apply {
                setText(state.text.value())
                if (!subcomposing) {
                    setSelection(state.selection.first, state.selection.last)
                    setOnFocusChangeListener { _, hasFocus ->
                        state.hasFocus = hasFocus
                    }
                    addTextChangedListener { editable ->
                        coroutineScope.launch(Dispatchers.Main) {
                            onTyping(!editable.isNullOrEmpty())
                            state.text.update(editable, false)
                            state.lineCount = lineCount

                            state.currentMentionSuggestion = editable?.checkSuggestionNeeded()
                            onSuggestionReceived(state.currentMentionSuggestion)
                        }
                    }
                    onSelectionChangeListener = { selStart, selEnd ->
                        state.selection = selStart..selEnd
                        state.currentMentionSuggestion = editableText.checkSuggestionNeeded()
                        onSuggestionReceived(state.currentMentionSuggestion)
                    }
                    state.onRequestFocus = { this.requestFocus() }
                }
            }
        },
        update = { editText ->
            if (state.text.needsDisplaying()) {
                editText.editableText.clear()
                editText.editableText.append(state.text.value())
            }
            if ((editText.selectionStart != state.selection.first && state.selection.first in 0..editText.editableText.length ||
                    editText.selectionEnd != state.selection.last) && state.selection.last in 0..editText.editableText.length) {
                println("Changing selection to ${state.selection}")
                editText.setSelection(state.selection.first, state.selection.last)
            }
        }
    )
}

private fun Editable.checkSuggestionNeeded(): Suggestion? {
    if (this.isEmpty()) return null
    val start = Selection.getSelectionStart(this)
    val end = Selection.getSelectionEnd(this)
    var startOfWord = start
    while ((startOfWord > 0 || startOfWord == length) && !this[startOfWord - 1].isWhitespace()) {
        startOfWord--
    }
    if (startOfWord !in indices) return null
    val firstChar = this[startOfWord]
    if (getSpans<MentionSpan>(startOfWord, startOfWord + 1).isNotEmpty()) return null
    return if (firstChar in listOf('@', '#', "/")) {
        var endOfWord = end
        while (endOfWord < this.length && !this[endOfWord].isWhitespace()) {
            endOfWord++
        }
        val text = this.subSequence(startOfWord + 1, endOfWord).toString()
        val suggestionType = when (firstChar) {
            '@' -> SuggestionType.Mention
            '#' -> SuggestionType.Room
            '/' -> SuggestionType.Command
            else -> return null
        }
        Suggestion(startOfWord, endOfWord, suggestionType, text)
    } else {
        null
    }
}

class MarkdownEditText(
    context: Context,
) : EditText(context) {
    var onSelectionChangeListener: ((Int, Int) -> Unit)? = null

    private var isModifyingText = false

    override fun setText(text: CharSequence?, type: BufferType?) {
        isModifyingText = true
        super.setText(text, type)
        isModifyingText = false
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!isModifyingText) {
            onSelectionChangeListener?.invoke(selStart, selEnd)
        }
    }
}

class ImmutableCharSequence(initialText: CharSequence = "") {
    private var value by mutableStateOf<SpannableString>(SpannableString(initialText))
    private var needsDisplaying by mutableStateOf(false)

    fun update(newText: CharSequence?, needsDisplaying: Boolean) {
        value = SpannableString(newText.orEmpty())
        this.needsDisplaying = needsDisplaying
    }

    fun value(): CharSequence = value
    fun needsDisplaying(): Boolean = needsDisplaying
}
