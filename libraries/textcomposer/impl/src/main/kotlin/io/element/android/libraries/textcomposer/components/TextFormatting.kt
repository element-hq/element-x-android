/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.textcomposer.TextComposerLinkDialog
import io.element.android.libraries.textcomposer.model.aRichTextEditorState
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
            toggleable = true,
            onClick = { onInlineFormatClick(InlineFormat.Bold) },
            imageVector = CompoundIcons.Bold(),
            contentDescription = stringResource(R.string.rich_text_editor_format_bold)
        )
        FormattingOption(
            state = state.actions[ComposerAction.ITALIC].toButtonState(),
            toggleable = true,
            onClick = { onInlineFormatClick(InlineFormat.Italic) },
            imageVector = CompoundIcons.Italic(),
            contentDescription = stringResource(R.string.rich_text_editor_format_italic)
        )
        FormattingOption(
            state = state.actions[ComposerAction.UNDERLINE].toButtonState(),
            toggleable = true,
            onClick = { onInlineFormatClick(InlineFormat.Underline) },
            imageVector = CompoundIcons.Underline(),
            contentDescription = stringResource(R.string.rich_text_editor_format_underline)
        )
        FormattingOption(
            state = state.actions[ComposerAction.STRIKE_THROUGH].toButtonState(),
            toggleable = true,
            onClick = { onInlineFormatClick(InlineFormat.StrikeThrough) },
            imageVector = CompoundIcons.Strikethrough(),
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
            toggleable = true,
            onClick = { linkDialogAction = state.linkAction },
            imageVector = CompoundIcons.Link(),
            contentDescription = stringResource(R.string.rich_text_editor_link)
        )

        FormattingOption(
            state = state.actions[ComposerAction.UNORDERED_LIST].toButtonState(),
            toggleable = true,
            onClick = { onToggleListClick(ordered = false) },
            imageVector = CompoundIcons.ListBulleted(),
            contentDescription = stringResource(R.string.rich_text_editor_bullet_list)
        )
        FormattingOption(
            state = state.actions[ComposerAction.ORDERED_LIST].toButtonState(),
            toggleable = true,
            onClick = { onToggleListClick(ordered = true) },
            imageVector = CompoundIcons.ListNumbered(),
            contentDescription = stringResource(R.string.rich_text_editor_numbered_list)
        )
        FormattingOption(
            state = state.actions[ComposerAction.INDENT].toButtonState(),
            toggleable = false,
            onClick = { onIndentClick() },
            imageVector = CompoundIcons.IndentIncrease(),
            contentDescription = stringResource(R.string.rich_text_editor_indent)
        )
        FormattingOption(
            state = state.actions[ComposerAction.UNINDENT].toButtonState(),
            toggleable = false,
            onClick = { onUnindentClick() },
            imageVector = CompoundIcons.IndentDecrease(),
            contentDescription = stringResource(R.string.rich_text_editor_unindent)
        )
        FormattingOption(
            state = state.actions[ComposerAction.INLINE_CODE].toButtonState(),
            toggleable = true,
            onClick = { onInlineFormatClick(InlineFormat.InlineCode) },
            imageVector = CompoundIcons.InlineCode(),
            contentDescription = stringResource(R.string.rich_text_editor_inline_code)
        )
        FormattingOption(
            state = state.actions[ComposerAction.CODE_BLOCK].toButtonState(),
            toggleable = true,
            onClick = { onCodeBlockClick() },
            imageVector = CompoundIcons.Code(),
            contentDescription = stringResource(R.string.rich_text_editor_code_block)
        )
        FormattingOption(
            state = state.actions[ComposerAction.QUOTE].toButtonState(),
            toggleable = true,
            onClick = { onQuoteClick() },
            imageVector = CompoundIcons.Quote(),
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
    TextFormatting(state = aRichTextEditorState())
}
