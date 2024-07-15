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

package io.element.android.libraries.textcomposer.components.markdown

import android.content.ClipData
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.Selection
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.getSpans
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.updateMentionStyles
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.wysiwyg.compose.RichTextEditorStyle
import io.element.android.wysiwyg.compose.internal.applyStyleInCompose

@Suppress("ModifierMissing")
@Composable
fun MarkdownTextInput(
    state: MarkdownTextEditorState,
    subcomposing: Boolean,
    onTyping: (Boolean) -> Unit,
    onReceiveSuggestion: (Suggestion?) -> Unit,
    richTextEditorStyle: RichTextEditorStyle,
    onSelectRichContent: ((Uri) -> Unit)?,
) {
    val canUpdateState = !subcomposing

    // Copied from io.element.android.wysiwyg.internal.utils.UriContentListener
    class ReceiveUriContentListener(
        private val onContent: (uri: Uri) -> Unit,
    ) : OnReceiveContentListener {
        override fun onReceiveContent(view: View, payload: ContentInfoCompat): ContentInfoCompat? {
            val split = payload.partition { item -> item.uri != null }
            val uriContent = split.first
            val remaining = split.second

            if (uriContent != null) {
                val clip: ClipData = uriContent.clip
                for (i in 0 until clip.itemCount) {
                    val uri = clip.getItemAt(i).uri
                    // ... app-specific logic to handle the URI ...
                    onContent(uri)
                }
            }
            // Return anything that we didn't handle ourselves. This preserves the default platform
            // behavior for text and anything else for which we are not implementing custom handling.
            return remaining
        }
    }

    val mentionSpanTheme = LocalMentionSpanTheme.current

    AndroidView(
        modifier = Modifier
            .padding(top = 6.dp, bottom = 6.dp)
            .fillMaxWidth(),
        factory = { context ->
            MarkdownEditText(context).apply {
                tag = TestTags.plainTextEditor.value // Needed for UI tests
                setPadding(0)
                setBackgroundColor(Color.TRANSPARENT)
                val text = state.text.value()
                setText(text)
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                if (canUpdateState) {
                    val textRange = 0..text.length
                    setSelection(state.selection.first.coerceIn(textRange), state.selection.last.coerceIn(textRange))
                    setOnFocusChangeListener { _, hasFocus ->
                        state.hasFocus = hasFocus
                    }
                    addTextChangedListener { editable ->
                        onTyping(!editable.isNullOrEmpty())
                        state.text.update(editable, false)
                        state.lineCount = lineCount

                        state.currentMentionSuggestion = editable?.checkSuggestionNeeded()
                        onReceiveSuggestion(state.currentMentionSuggestion)
                    }
                    onSelectionChangeListener = { selStart, selEnd ->
                        state.selection = selStart..selEnd
                        state.currentMentionSuggestion = editableText.checkSuggestionNeeded()
                        onReceiveSuggestion(state.currentMentionSuggestion)
                    }
                    if (onSelectRichContent != null) {
                        ViewCompat.setOnReceiveContentListener(
                            this,
                            arrayOf("image/*"),
                            ReceiveUriContentListener { onSelectRichContent(it) }
                        )
                    }
                    state.requestFocusAction = { this.requestFocus() }
                }
            }
        },
        update = { editText ->
            editText.applyStyleInCompose(richTextEditorStyle)

            if (state.text.needsDisplaying()) {
                val text = state.text.value()
                mentionSpanTheme.updateMentionStyles(text)
                editText.updateEditableText(text)
                if (canUpdateState) {
                    state.text.update(editText.editableText, false)
                }
            }
            if (canUpdateState) {
                val newSelectionStart = state.selection.first
                val newSelectionEnd = state.selection.last
                val currentTextRange = 0..editText.editableText.length
                val didSelectionChange = { editText.selectionStart != newSelectionStart || editText.selectionEnd != newSelectionEnd }
                val isNewSelectionValid = { newSelectionStart in currentTextRange && newSelectionEnd in currentTextRange }
                if (didSelectionChange() && isNewSelectionValid()) {
                    editText.setSelection(state.selection.first, state.selection.last)
                }
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

    // If a mention span already exists we don't need suggestions
    if (getSpans<MentionSpan>(startOfWord, startOfWord + 1).isNotEmpty()) return null

    return if (firstChar in listOf('@', '#', '/')) {
        var endOfWord = end
        while (endOfWord < this.length && !this[endOfWord].isWhitespace()) {
            endOfWord++
        }
        val text = this.subSequence(startOfWord + 1, endOfWord).toString()
        val suggestionType = when (firstChar) {
            '@' -> SuggestionType.Mention
            '#' -> SuggestionType.Room
            '/' -> SuggestionType.Command
            else -> error("Unknown suggestion type. This should never happen.")
        }
        Suggestion(startOfWord, endOfWord, suggestionType, text)
    } else {
        null
    }
}

@PreviewsDayNight
@Composable
internal fun MarkdownTextInputPreview() {
    ElementPreview {
        val style = ElementRichTextEditorStyle.composerStyle(hasFocus = true)
        MarkdownTextInput(
            state = aMarkdownTextEditorState(),
            subcomposing = false,
            onTyping = {},
            onReceiveSuggestion = {},
            richTextEditorStyle = style,
            onSelectRichContent = {},
        )
    }
}

internal fun aMarkdownTextEditorState(
    initialText: String = "Hello, World!",
    initialFocus: Boolean = true,
) = MarkdownTextEditorState(
    initialText = initialText,
    initialFocus = initialFocus,
)
