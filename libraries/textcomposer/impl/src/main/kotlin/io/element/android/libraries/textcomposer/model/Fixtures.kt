/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import io.element.android.wysiwyg.compose.RichTextEditorState

fun aTextEditorStateMarkdown(
    initialText: String? = "",
    initialFocus: Boolean = false,
    isRoomEncrypted: Boolean? = null,
): TextEditorState {
    return TextEditorState.Markdown(
        aMarkdownTextEditorState(
            initialText = initialText,
            initialFocus = initialFocus,
        ),
        isRoomEncrypted = isRoomEncrypted,
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
    isRoomEncrypted: Boolean? = null,
): TextEditorState {
    return TextEditorState.Rich(
        aRichTextEditorState(
            initialText = initialText,
            initialHtml = initialHtml,
            initialMarkdown = initialMarkdown,
            initialFocus = initialFocus,
        ),
        isRoomEncrypted = isRoomEncrypted,
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
