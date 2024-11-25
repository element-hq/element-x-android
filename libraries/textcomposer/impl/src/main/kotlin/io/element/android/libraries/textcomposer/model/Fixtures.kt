/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import io.element.android.wysiwyg.compose.RichTextEditorState

fun aTextEditorStateMarkdown(
    initialText: String? = "",
    initialFocus: Boolean = false,
): TextEditorState {
    return TextEditorState.Markdown(
        aMarkdownTextEditorState(
            initialText = initialText,
            initialFocus = initialFocus,
        )
    )
}

fun aMarkdownTextEditorState(
    initialText: String? = "",
    initialFocus: Boolean = false,
): MarkdownTextEditorState {
    return MarkdownTextEditorState(
        initialText = initialText,
        initialFocus = initialFocus,
    )
}

fun aTextEditorStateRich(
    initialText: String = "",
    initialHtml: String = initialText,
    initialMarkdown: String = initialText,
    initialFocus: Boolean = false,
): TextEditorState {
    return TextEditorState.Rich(
        aRichTextEditorState(
            initialText = initialText,
            initialHtml = initialHtml,
            initialMarkdown = initialMarkdown,
            initialFocus = initialFocus,
        )
    )
}

fun aRichTextEditorState(
    initialText: String = "",
    initialHtml: String = initialText,
    initialMarkdown: String = initialText,
    initialFocus: Boolean = false,
): RichTextEditorState {
    return RichTextEditorState(
        initialHtml = initialHtml,
        initialMarkdown = initialMarkdown,
        initialFocus = initialFocus,
    )
}
