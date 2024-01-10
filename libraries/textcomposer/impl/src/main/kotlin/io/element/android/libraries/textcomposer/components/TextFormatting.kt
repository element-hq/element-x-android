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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.textcomposer.TextComposerLinkDialog
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.view.models.InlineFormat
import io.element.android.wysiwyg.view.models.LinkAction
import kotlinx.coroutines.launch
import uniffi.wysiwyg_composer.ActionState
import uniffi.wysiwyg_composer.ComposerAction
@Composable
internal fun TextFormatting(
    state: RichTextEditorState,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    fun onInlineFormatClick(inlineFormat: InlineFormat) {
        coroutineScope.launch {
            state.toggleInlineFormat(inlineFormat)
        }
    }

    fun onToggleListClick(ordered: Boolean) {
        coroutineScope.launch {
            state.toggleList(ordered)
        }
    }

    fun onIndentClick() {
        coroutineScope.launch {
            state.indent()
        }
    }

    fun onUnindentClick() {
        coroutineScope.launch {
            state.unindent()
        }
    }

    fun onCodeBlockClick() {
        coroutineScope.launch {
            state.toggleCodeBlock()
        }
    }

    fun onQuoteClick() {
        coroutineScope.launch {
            state.toggleQuote()
        }
    }

    fun onCreateLinkRequest(url: String, text: String) {
        coroutineScope.launch {
            state.insertLink(url, text)
        }
    }

    fun onSaveLinkRequest(url: String) {
        coroutineScope.launch {
            state.setLink(url)
        }
    }

    fun onRemoveLinkRequest() {
        coroutineScope.launch {
            state.removeLink()
        }
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FormattingOption(
            state = state.actions[ComposerAction.BOLD].toButtonState(),
            onClick = { onInlineFormatClick(InlineFormat.Bold) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_bold),
            contentDescription = stringResource(R.string.rich_text_editor_format_bold)
        )
        FormattingOption(
            state = state.actions[ComposerAction.ITALIC].toButtonState(),
            onClick = { onInlineFormatClick(InlineFormat.Italic) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_italic),
            contentDescription = stringResource(R.string.rich_text_editor_format_italic)
        )
        FormattingOption(
            state = state.actions[ComposerAction.UNDERLINE].toButtonState(),
            onClick = { onInlineFormatClick(InlineFormat.Underline) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_underline),
            contentDescription = stringResource(R.string.rich_text_editor_format_underline)
        )
        FormattingOption(
            state = state.actions[ComposerAction.STRIKE_THROUGH].toButtonState(),
            onClick = { onInlineFormatClick(InlineFormat.StrikeThrough) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_strikethrough),
            contentDescription = stringResource(R.string.rich_text_editor_format_strikethrough)
        )

        var linkDialogAction by remember { mutableStateOf<LinkAction?>(null) }

        linkDialogAction?.let {
            TextComposerLinkDialog(
                onDismissRequest = { linkDialogAction = null },
                onCreateLinkRequest = ::onCreateLinkRequest,
                onSaveLinkRequest = ::onSaveLinkRequest,
                onRemoveLinkRequest = ::onRemoveLinkRequest,
                linkAction = it,
            )
        }

        FormattingOption(
            state = state.actions[ComposerAction.LINK].toButtonState(),
            onClick = { linkDialogAction = state.linkAction },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_link),
            contentDescription = stringResource(R.string.rich_text_editor_link)
        )

        FormattingOption(
            state = state.actions[ComposerAction.UNORDERED_LIST].toButtonState(),
            onClick = { onToggleListClick(ordered = false) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_bullet_list),
            contentDescription = stringResource(R.string.rich_text_editor_bullet_list)
        )
        FormattingOption(
            state = state.actions[ComposerAction.ORDERED_LIST].toButtonState(),
            onClick = { onToggleListClick(ordered = true) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_numbered_list),
            contentDescription = stringResource(R.string.rich_text_editor_numbered_list)
        )
        FormattingOption(
            state = state.actions[ComposerAction.INDENT].toButtonState(),
            onClick = { onIndentClick() },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_indent_increase),
            contentDescription = stringResource(R.string.rich_text_editor_indent)
        )
        FormattingOption(
            state = state.actions[ComposerAction.UNINDENT].toButtonState(),
            onClick = { onUnindentClick() },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_indent_decrease),
            contentDescription = stringResource(R.string.rich_text_editor_unindent)
        )
        FormattingOption(
            state = state.actions[ComposerAction.INLINE_CODE].toButtonState(),
            onClick = { onInlineFormatClick(InlineFormat.InlineCode) },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_inline_code),
            contentDescription = stringResource(R.string.rich_text_editor_inline_code)
        )
        FormattingOption(
            state = state.actions[ComposerAction.CODE_BLOCK].toButtonState(),
            onClick = { onCodeBlockClick() },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_code_block),
            contentDescription = stringResource(R.string.rich_text_editor_code_block)
        )
        FormattingOption(
            state = state.actions[ComposerAction.QUOTE].toButtonState(),
            onClick = { onQuoteClick() },
            imageVector = ImageVector.vectorResource(CommonDrawables.ic_quote),
            contentDescription = stringResource(R.string.rich_text_editor_quote)
        )
    }
}

private fun ActionState?.toButtonState(): FormattingOptionState =
    when (this) {
        ActionState.ENABLED -> FormattingOptionState.Default
        ActionState.REVERSED -> FormattingOptionState.Selected
        ActionState.DISABLED, null -> FormattingOptionState.Disabled
    }

@PreviewsDayNight
@Composable
internal fun TextFormattingPreview() = ElementPreview {
    TextFormatting(state = RichTextEditorState())
}
