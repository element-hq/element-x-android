/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.Composable
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.compose.rememberRichTextEditorState

class TestRichTextEditorStateFactory : RichTextEditorStateFactory {
    @Composable
    override fun remember(): RichTextEditorState {
        return rememberRichTextEditorState("", fake = true)
    }
}
