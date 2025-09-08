/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.compose.rememberRichTextEditorState

interface RichTextEditorStateFactory {
    @Composable
    fun remember(): RichTextEditorState
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultRichTextEditorStateFactory : RichTextEditorStateFactory {
    @Composable
    override fun remember(): RichTextEditorState {
        return rememberRichTextEditorState()
    }
}
