/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.wysiwyg.compose.RichTextEditorState

@Immutable
sealed interface TextEditorState {
    data class Markdown(
        val state: MarkdownTextEditorState,
    ) : TextEditorState

    data class Rich(
        val richTextEditorState: RichTextEditorState
    ) : TextEditorState

    fun messageHtml(): String? = when (this) {
        is Markdown -> null
        is Rich -> richTextEditorState.messageHtml
    }

    fun messageMarkdown(permalinkBuilder: PermalinkBuilder): String = when (this) {
        is Markdown -> state.getMessageMarkdown(permalinkBuilder)
        is Rich -> richTextEditorState.messageMarkdown
    }

    fun hasFocus(): Boolean = when (this) {
        is Markdown -> state.hasFocus
        is Rich -> richTextEditorState.hasFocus
    }

    suspend fun setHtml(html: String) {
        when (this) {
            is Markdown -> Unit
            is Rich -> richTextEditorState.setHtml(html)
        }
    }

    suspend fun setMarkdown(text: String) {
        when (this) {
            is Markdown -> state.text.update(text, true)
            is Rich -> richTextEditorState.setMarkdown(text)
        }
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
            is Markdown -> state.requestFocusAction()
            is Rich -> richTextEditorState.requestFocus()
        }
    }

    val lineCount: Int get() = when (this) {
        is Markdown -> state.lineCount
        is Rich -> richTextEditorState.lineCount
    }
}
