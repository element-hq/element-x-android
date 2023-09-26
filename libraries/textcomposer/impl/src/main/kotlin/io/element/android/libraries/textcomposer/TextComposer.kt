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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.textcomposer.components.FormattingOption
import io.element.android.libraries.textcomposer.components.FormattingOptionState
import io.element.android.libraries.textcomposer.components.textInputRoundedCornerShape
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditor
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.view.models.InlineFormat
import io.element.android.wysiwyg.view.models.LinkAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import uniffi.wysiwyg_composer.ActionState
import uniffi.wysiwyg_composer.ComposerAction

@Composable
fun TextComposer(
    state: RichTextEditorState,
    composerMode: MessageComposerMode,
    enableTextFormatting: Boolean,
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
    subcomposing: Boolean = false,
    onRequestFocus: () -> Unit = {},
    onSendMessage: (Message) -> Unit = {},
    onResetComposerMode: () -> Unit = {},
    onAddAttachment: () -> Unit = {},
    onDismissTextFormatting: () -> Unit = {},
    onError: (Throwable) -> Unit = {},
) {
    val onSendClicked = {
        val html = if (enableTextFormatting) state.messageHtml else null
        onSendMessage(Message(html = html, markdown = state.messageMarkdown))
    }

    val layoutModifier = modifier
        .fillMaxSize()
        .height(IntrinsicSize.Min)

    val composerOptionsButton = @Composable {
        ComposerOptionsButton(
            modifier = Modifier
                .size(48.dp),
            onClick = onAddAttachment
        )
    }

    val textInput = @Composable {
        TextInput(
            state = state,
            subcomposing = subcomposing,
            placeholder = if (composerMode.inThread) {
                stringResource(id = CommonStrings.action_reply_in_thread)
            } else {
                stringResource(id = R.string.rich_text_editor_composer_placeholder)
            },
            composerMode = composerMode,
            onResetComposerMode = onResetComposerMode,
            onError = onError,
        )
    }

    val sendButton = @Composable {
        SendButton(
            canSendMessage = state.messageHtml.isNotEmpty(),
            onClick = onSendClicked,
            composerMode = composerMode,
        )
    }

    val textFormattingOptions = @Composable { TextFormatting(state = state) }

    if (showTextFormatting) {
        TextFormattingLayout(
            modifier = layoutModifier,
            textInput = textInput,
            dismissTextFormattingButton = {
                DismissTextFormattingButton(onClick = onDismissTextFormatting)
            },
            textFormatting = textFormattingOptions,
            sendButton = sendButton
        )
    } else {
        StandardLayout(
            modifier = layoutModifier,
            composerOptionsButton = composerOptionsButton,
            textInput = textInput,
            sendButton = sendButton
        )
    }

    if (!subcomposing) {
        SoftKeyboardEffect(composerMode, onRequestFocus) {
            it is MessageComposerMode.Special
        }

        SoftKeyboardEffect(showTextFormatting, onRequestFocus) { it }
    }
}

@Composable
private fun StandardLayout(
    textInput: @Composable () -> Unit,
    composerOptionsButton: @Composable () -> Unit,
    sendButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            Modifier
                .padding(bottom = 5.dp, top = 5.dp, start = 3.dp)
        ) {
            composerOptionsButton()
        }
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp)
                .weight(1f)
        ) {
            textInput()
        }
        Box(
            Modifier
                .padding(bottom = 5.dp, top = 5.dp, end = 6.dp, start = 6.dp)
        ) {
            sendButton()
        }
    }
}

@Composable
private fun TextFormattingLayout(
    textInput: @Composable () -> Unit,
    dismissTextFormattingButton: @Composable () -> Unit,
    textFormatting: @Composable () -> Unit,
    sendButton: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            textInput()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.padding(start = 3.dp)
            ) {
                dismissTextFormattingButton()
            }
            Box(modifier = Modifier.weight(1f)) {
                textFormatting()
            }
            Box(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = 6.dp
                )
            ) {
                sendButton()
            }
        }
    }
}

@Composable
private fun TextInput(
    state: RichTextEditorState,
    subcomposing: Boolean,
    placeholder: String,
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
) {
    val bgColor = ElementTheme.colors.bgSubtleSecondary
    val borderColor = ElementTheme.colors.borderDisabled
    val roundedCorners = textInputRoundedCornerShape(composerMode = composerMode)

    Column(
        modifier = modifier
            .clip(roundedCorners)
            .border(0.5.dp, borderColor, roundedCorners)
            .background(color = bgColor)
            .requiredHeightIn(min = 42.dp.applyScaleUp())
            .fillMaxSize(),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
        }
        val defaultTypography = ElementTheme.typography.fontBodyLgRegular
        Box(
            modifier = Modifier
                .padding(
                    top = 4.dp.applyScaleUp(),
                    bottom = 4.dp.applyScaleUp(),
                    start = 12.dp.applyScaleUp(),
                    end = 42.dp.applyScaleUp()
                )
                .testTag(TestTags.richTextEditor),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Placeholder
            if (state.messageHtml.isEmpty()) {
                Text(
                    placeholder,
                    style = defaultTypography.copy(
                        color = ElementTheme.colors.textSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            RichTextEditor(
                state = state,
                registerStateUpdates = !subcomposing,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                style = ElementRichTextEditorStyle.create(
                    hasFocus = state.hasFocus
                ),
                onError = onError
            )
        }
    }
}

@Composable
private fun ComposerOptionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(30.dp.applyScaleUp()),
            resourceId = CommonDrawables.ic_plus,
            contentDescription = stringResource(R.string.rich_text_editor_a11y_add_attachment),
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@Composable
private fun DismissTextFormattingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(30.dp.applyScaleUp()),
            resourceId = CommonDrawables.ic_cancel,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.colors.iconPrimary,
        )
    }
}

@Composable
private fun TextFormatting(
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

@Composable
private fun ComposerModeView(
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (composerMode) {
        is MessageComposerMode.Edit -> {
            EditingModeView(onResetComposerMode = onResetComposerMode, modifier = modifier)
        }
        is MessageComposerMode.Reply -> {
            ReplyToModeView(
                modifier = modifier.padding(8.dp),
                senderName = composerMode.senderName,
                text = composerMode.defaultContent,
                attachmentThumbnailInfo = composerMode.attachmentThumbnailInfo,
                onResetComposerMode = onResetComposerMode,
            )
        }
        else -> Unit
    }
}

@Composable
private fun EditingModeView(
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        Icon(
            resourceId = CommonDrawables.ic_september_edit_solid_16,
            contentDescription = stringResource(CommonStrings.common_editing),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(16.dp.applyScaleUp()),
        )
        Text(
            stringResource(CommonStrings.common_editing),
            style = ElementTheme.typography.fontBodySmRegular,
            textAlign = TextAlign.Start,
            color = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )
        Icon(
            resourceId = CommonDrawables.ic_compound_close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 12.dp)
                .size(16.dp.applyScaleUp())
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@Composable
private fun ReplyToModeView(
    senderName: String,
    text: String?,
    attachmentThumbnailInfo: AttachmentThumbnailInfo?,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        if (attachmentThumbnailInfo != null) {
            AttachmentThumbnail(
                info = attachmentThumbnailInfo,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = senderName,
                modifier = Modifier.fillMaxWidth(),
                style = ElementTheme.typography.fontBodySmMedium,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.primary,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text.orEmpty(),
                style = ElementTheme.typography.fontBodyMdRegular,
                textAlign = TextAlign.Start,
                color = ElementTheme.materialColors.secondary,
                maxLines = if (attachmentThumbnailInfo != null) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            resourceId = CommonDrawables.ic_compound_close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(end = 4.dp, top = 4.dp, start = 16.dp, bottom = 16.dp)
                .size(16.dp.applyScaleUp())
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@Composable
private fun SendButton(
    canSendMessage: Boolean,
    onClick: () -> Unit,
    composerMode: MessageComposerMode,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .size(48.dp.applyScaleUp()),
        onClick = onClick,
        enabled = canSendMessage,
    ) {
        val iconId = when (composerMode) {
            is MessageComposerMode.Edit -> CommonDrawables.ic_compound_check
            else -> CommonDrawables.ic_september_send
        }
        val iconSize = when (composerMode) {
            is MessageComposerMode.Edit -> 24.dp
            // CommonDrawables.ic_september_send is too big... reduce its size.
            else -> 18.dp
        }
        val iconStartPadding = when (composerMode) {
            is MessageComposerMode.Edit -> 0.dp
            else -> 2.dp
        }
        val contentDescription = when (composerMode) {
            is MessageComposerMode.Edit -> stringResource(CommonStrings.action_edit)
            else -> stringResource(CommonStrings.action_send)
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp.applyScaleUp())
                .background(if (canSendMessage) ElementTheme.colors.iconAccentTertiary else Color.Transparent)
        ) {
            Icon(
                modifier = Modifier
                    .height(iconSize.applyScaleUp())
                    .padding(start = iconStartPadding)
                    .align(Alignment.Center),
                resourceId = iconId,
                contentDescription = contentDescription,
                // Exception here, we use Color.White instead of ElementTheme.colors.iconOnSolidPrimary
                tint = if (canSendMessage) Color.White else ElementTheme.colors.iconDisabled
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    PreviewColumn(items = persistentListOf(
        {
            TextComposer(
                RichTextEditorState("", initialFocus = true),
                onSendMessage = {},
                composerMode = MessageComposerMode.Normal(""),
                onResetComposerMode = {},
                enableTextFormatting = true,
            )
        }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState(
                "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
                initialFocus = true
            ),
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message without focus", initialFocus = false),
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    })
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState("", initialFocus = false),
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = false),
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message\nWith several lines\nTo preview larger textfields and long lines with overflow", initialFocus = false),
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            onSendMessage = {},
            composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text", TransactionId("1234")),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerReplyPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        TextComposer(
            RichTextEditorState(""),
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = null,
                defaultContent = "A message\n" +
                    "With several lines\n" +
                    "To preview larger textfields and long lines with overflow"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    },
        {
            TextComposer(
                RichTextEditorState(""),
                onSendMessage = {},
                composerMode = MessageComposerMode.Reply(
                    isThreaded = true,
                    senderName = "Alice",
                    eventId = EventId("$1234"),
                    attachmentThumbnailInfo = null,
                    defaultContent = "A message\n" +
                        "With several lines\n" +
                        "To preview larger textfields and long lines with overflow"
                ),
                onResetComposerMode = {},
                enableTextFormatting = true,
            )
        }, {
        TextComposer(
            RichTextEditorState("A message"),
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = true,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = MediaSource("https://domain.com/image.jpg"),
                    textContent = "image.jpg",
                    type = AttachmentThumbnailType.Image,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "image.jpg"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message"),
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = MediaSource("https://domain.com/video.mp4"),
                    textContent = "video.mp4",
                    type = AttachmentThumbnailType.Video,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "video.mp4"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message"),
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = null,
                    textContent = "logs.txt",
                    type = AttachmentThumbnailType.File,
                    blurHash = null,
                ),
                defaultContent = "logs.txt"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }, {
        TextComposer(
            RichTextEditorState("A message", initialFocus = true),
            onSendMessage = {},
            composerMode = MessageComposerMode.Reply(
                isThreaded = false,
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    thumbnailSource = null,
                    textContent = null,
                    type = AttachmentThumbnailType.Location,
                    blurHash = null,
                ),
                defaultContent = "Shared location"
            ),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    })
    )
}

@Composable
private fun PreviewColumn(
    items: ImmutableList<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                item()
            }
        }
    }
}
