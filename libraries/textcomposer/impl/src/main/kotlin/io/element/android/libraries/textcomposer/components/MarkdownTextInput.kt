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
import android.graphics.Color
import android.text.Editable
import android.text.Selection
import android.text.SpannableString
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.getSpans
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.textcomposer.ComposerModeView
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditorStyle
import io.element.android.wysiwyg.compose.internal.applyStyleInCompose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("ModifierMissing")
@Composable
fun MarkdownTextInput(
    state: MarkdownTextEditorState,
    subcomposing: Boolean,
    onTyping: (Boolean) -> Unit,
    onSuggestionReceived: (Suggestion?) -> Unit,
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    richTextEditorStyle: RichTextEditorStyle,
) {
    val coroutineScope = rememberCoroutineScope()

    val bgColor = ElementTheme.colors.bgSubtleSecondary
    val borderColor = ElementTheme.colors.borderDisabled
    val roundedCorners = textInputRoundedCornerShape(composerMode = composerMode)

    LaunchedEffect(composerMode) {
        if (composerMode is MessageComposerMode.Edit) {
            state.text.update(composerMode.defaultContent, true)
        }
    }

    Column(
        modifier = Modifier
            .clip(roundedCorners)
            .border(0.5.dp, borderColor, roundedCorners)
            .background(color = bgColor)
            .requiredHeightIn(min = 42.dp)
            .fillMaxSize(),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
        }
        val defaultTypography = ElementTheme.typography.fontBodyLgRegular
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 42.dp)
                .testTag(TestTags.richTextEditor),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Placeholder
            if (state.text.value().isEmpty()) {
                Text(
                    text = if (composerMode.inThread) {
                        stringResource(id = CommonStrings.action_reply_in_thread)
                    } else {
                        stringResource(id = R.string.rich_text_editor_composer_placeholder)
                    },
                    style = defaultTypography.copy(
                        color = ElementTheme.colors.textSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            AndroidView(
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                factory = { context ->
                    MarkdownEditText(context).apply {
                        setPadding(0)
                        setBackgroundColor(Color.TRANSPARENT)
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
                            state.requestFocusAction = { this.requestFocus() }
                        }
                    }
                },
                update = { editText ->
                    editText.applyStyleInCompose(richTextEditorStyle)

                    if (state.text.needsDisplaying()) {
                        editText.editableText.clear()
                        editText.editableText.append(state.text.value())
                    }
                    val newSelectionStart = state.selection.first
                    val newSelectionEnd = state.selection.last
                    val currentTextRange = 0..editText.editableText.length
                    val didSelectionChange = { editText.selectionStart != newSelectionStart || editText.selectionEnd != newSelectionEnd }
                    val isNewSelectionValid = { newSelectionStart in currentTextRange && newSelectionEnd in currentTextRange }
                    if (didSelectionChange() && isNewSelectionValid()) {
                        editText.setSelection(state.selection.first, state.selection.last)
                    }
                }
            )
        }
    }
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
) : AppCompatEditText(context) {
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
