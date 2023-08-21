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

package io.element.android.libraries.textcomposer

import androidx.compose.runtime.Composable
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.compose.rememberRichTextEditorState

data class TextComposerState constructor(
    internal val inner: InnerTextComposerState
) : InnerTextComposerState {
    override val messageHtml: String
        get() = inner.messageHtml

    override val messageMarkdown: String
        get() = inner.messageMarkdown

    override val hasFocus: Boolean
        get() = inner.hasFocus

    override val lineCount: Int
        get() = inner.lineCount

    override val canSendMessage: Boolean
        get() = messageHtml.isNotEmpty()

    override fun setHtml(value: String) {
        inner.setHtml(value)
    }

    override fun requestFocus(): Boolean =
        inner.requestFocus()
}

interface InnerTextComposerState {
    val messageHtml: String
    val messageMarkdown: String
    val hasFocus: Boolean
    val lineCount: Int
    val canSendMessage: Boolean

    fun setHtml(value: String)
    fun requestFocus(): Boolean
}

data class RealInnerTextComposerState internal constructor(
    internal val richTextEditorState: RichTextEditorState
) : InnerTextComposerState {
    override val messageHtml: String
        get() = richTextEditorState.messageHtml

    override val messageMarkdown: String
        get() = richTextEditorState.messageMarkdown

    override val hasFocus: Boolean
        get() = richTextEditorState.hasFocus

    override val lineCount: Int
        get() = richTextEditorState.lineCount

    override val canSendMessage: Boolean
        get() = messageHtml.isNotEmpty()

    override fun setHtml(value: String) {
        richTextEditorState.setHtml(value)
    }

    override fun requestFocus(): Boolean =
        richTextEditorState.requestFocus()
}

@Composable
fun rememberTextComposerState(): TextComposerState {
    val richTextEditorState = rememberRichTextEditorState()

    return TextComposerState(RealInnerTextComposerState(richTextEditorState))
}

fun previewTextComposerState(text: String): TextComposerState {
    val richTextEditorState = RichTextEditorState.createForLocalInspectionMode(text)

    return TextComposerState(RealInnerTextComposerState(richTextEditorState))
}
