/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components.markdown

import android.content.ClipData
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.Selection
import android.text.SpannableStringBuilder
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.core.text.getSpans
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import io.element.android.compound.theme.ElementTheme
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.allEmojis
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.libraries.textcomposer.model.aMarkdownTextEditorState
import io.element.android.wysiwyg.compose.RichTextEditorStyle
import io.element.android.wysiwyg.compose.internal.applyStyleInCompose
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Suppress("ModifierMissing")
@Composable
fun MarkdownTextInput(
    state: MarkdownTextEditorState,
    placeholder: String,
    placeholderColor: androidx.compose.ui.graphics.Color,
    onTyping: (Boolean) -> Unit,
    onReceiveSuggestion: (Suggestion?) -> Unit,
    richTextEditorStyle: RichTextEditorStyle,
    onSelectRichContent: ((Uri) -> Unit)?,
) {
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

    val mentionSpanUpdater = LocalMentionSpanUpdater.current

    var editTextLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val popUpPositionProvider = remember(editTextLayoutCoordinates) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val editTextPosition = editTextLayoutCoordinates?.positionInParent()
                return IntOffset(
                    x = anchorBounds.left + (editTextPosition?.x ?: 0f).toInt(),
                    y = anchorBounds.top + (editTextPosition?.y ?: 0f).toInt() - popupContentSize.height,
                ).also {
                    println("New offsets: $it. Window size: $windowSize, anchorBounds: $anchorBounds, popupContentSize: $popupContentSize")
                }
            }
        }
    }

    val context = LocalContext.current
    val emojiBase = remember { EmojibaseDatasource().load(context) }
    val currentSuggestion = state.currentSuggestion
    if (currentSuggestion?.type == SuggestionType.Emoji && currentSuggestion.text.isNotBlank()) {
        Popup(
            popupPositionProvider = popUpPositionProvider,
        ) {
            AnimatedVisibility(true) {
                Surface(
                    modifier = Modifier.widthIn(max = 320.dp).heightIn(min = 1.dp),
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 10.dp,
                ) {
                    val emojis by produceState(persistentListOf<String>(), currentSuggestion.text) {
                        value = emojiBase.allEmojis
                            .filter { emoji -> emoji.shortcodes.any { it.startsWith(currentSuggestion.text) } }
                            .take(10)
                            .map { it.unicode }
                            .toPersistentList()
                    }

                    LazyRow(
                        modifier = Modifier.widthIn(max = 320.dp),
                    ) {
                        items(items = emojis, key = { emoji -> emoji }) { emoji ->
                            Text(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                    .niceClickable {
                                        val newText = SpannableStringBuilder(state.text.value()).apply {
                                            replace(currentSuggestion.start, currentSuggestion.end, emoji)
                                        }
                                        state.text.update(newText = newText, needsDisplaying = true)
                                    },
                                text = emoji,
                                style = ElementTheme.typography.fontHeadingMdRegular,
                            )
                        }
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier
            .padding(top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
            .onPlaced { editTextLayoutCoordinates = it },
        factory = { context ->
            MarkdownEditText(context).apply {
                tag = TestTags.plainTextEditor.value // Needed for UI tests
                setPadding(0)
                setBackgroundColor(Color.TRANSPARENT)
                val text = state.text.value()
                setText(text)
                setHint(placeholder)
                setHintTextColor(ColorStateList.valueOf(placeholderColor.toArgb()))
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                val textRange = 0..text.length
                setSelection(state.selection.first.coerceIn(textRange), state.selection.last.coerceIn(textRange))
                setOnFocusChangeListener { _, hasFocus ->
                    state.hasFocus = hasFocus
                }
                addTextChangedListener { editable ->
                    onTyping(!editable.isNullOrEmpty())
                    state.text.update(editable, false)
                    state.lineCount = lineCount

                    state.currentSuggestion = editable?.checkSuggestionNeeded()
                    onReceiveSuggestion(state.currentSuggestion)
                }
                onSelectionChangeListener = { selStart, selEnd ->
                    state.selection = selStart..selEnd
                    state.currentSuggestion = editableText.checkSuggestionNeeded()
                    onReceiveSuggestion(state.currentSuggestion)
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
        },
        update = { editText ->
            editText.applyStyleInCompose(richTextEditorStyle)
            val text = state.text.value()
            mentionSpanUpdater.updateMentionSpans(text)
            if (state.text.needsDisplaying()) {
                editText.updateEditableText(text)
                state.text.update(editText.editableText, false)
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

    return if (firstChar in listOf('@', '#', '/', ':')) {
        var endOfWord = end
        while (endOfWord < this.length && !this[endOfWord].isWhitespace()) {
            endOfWord++
        }
        val text = this.subSequence(startOfWord + 1, endOfWord).toString()
        val suggestionType = when (firstChar) {
            '@' -> SuggestionType.Mention
            '#' -> SuggestionType.Room
            '/' -> SuggestionType.Command
            ':' -> SuggestionType.Emoji
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
            state = aMarkdownTextEditorState(initialText = "Hello, World!"),
            placeholder = "Placeholder",
            placeholderColor = ElementTheme.colors.textSecondary,
            onTyping = {},
            onReceiveSuggestion = {},
            richTextEditorStyle = style,
            onSelectRichContent = {},
        )
    }
}
