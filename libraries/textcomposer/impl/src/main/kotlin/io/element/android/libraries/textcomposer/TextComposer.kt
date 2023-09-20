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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension.Companion.fillToConstraints
import androidx.constraintlayout.compose.Visibility
import io.element.android.libraries.designsystem.preview.DayNightPreviews
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
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditor
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.view.models.InlineFormat
import io.element.android.wysiwyg.view.models.LinkAction
import uniffi.wysiwyg_composer.ActionState
import uniffi.wysiwyg_composer.ComposerAction

@Composable
fun TextComposer(
    state: RichTextEditorState,
    composerMode: MessageComposerMode,
    canSendMessage: Boolean,
    enableTextFormatting: Boolean,
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
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

    Column(
        modifier = modifier
            .padding(
                start = 3.dp,
                end = 6.dp,
                top = 8.dp,
                bottom = 4.dp,
            )
            .fillMaxWidth(),
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val (composeOptions, textInput, sendButton) = createRefs()
            val showComposerOptionsButton by remember(showTextFormatting) {
                derivedStateOf { !showTextFormatting }
            }
            IconButton(
                modifier = Modifier
                    .size(48.dp)
                    .constrainAs(composeOptions) {
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        visibility = if (showComposerOptionsButton) Visibility.Visible else Visibility.Gone
                    },
                onClick = onAddAttachment
            ) {
                Icon(
                    modifier = Modifier.size(30.dp.applyScaleUp()),
                    resourceId = CommonDrawables.ic_plus,
                    contentDescription = stringResource(R.string.rich_text_editor_a11y_add_attachment),
                    tint = ElementTheme.colors.iconPrimary,
                )
            }
            val roundCornerSmall = 20.dp.applyScaleUp()
            val roundCornerLarge = 28.dp.applyScaleUp()

            val roundedCornerSize = remember(state.lineCount, composerMode) {
                if (composerMode is MessageComposerMode.Special) {
                    roundCornerSmall
                } else {
                    roundCornerLarge
                }
            }
            val roundedCornerSizeState = animateDpAsState(
                targetValue = roundedCornerSize,
                animationSpec = tween(
                    durationMillis = 100,
                ),
                label = "roundedCornerSizeAnimation"
            )
            val roundedCorners = RoundedCornerShape(roundedCornerSizeState.value)
            val colors = ElementTheme.colors
            val bgColor = colors.bgSubtleSecondary
            val borderColor = colors.borderDisabled

            Column(
                modifier = Modifier
                    .constrainAs(textInput) {
                        start.linkTo(composeOptions.end, margin = 3.dp, goneMargin = 9.dp)
                        end.linkTo(sendButton.start, margin = 6.dp, goneMargin = 6.dp)
                        bottom.linkTo(parent.bottom)
                        width = fillToConstraints
                    }
                    .padding(vertical = 3.dp)
                    .fillMaxWidth()
                    .clip(roundedCorners)
                    .background(color = bgColor)
                    .border(0.5.dp, borderColor, roundedCorners)
            ) {
                if (composerMode is MessageComposerMode.Special) {
                    ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
                }
                TextInput(
                    state = state,
                    placeholder = if (composerMode.inThread) {
                        stringResource(id = CommonStrings.action_reply_in_thread)
                    } else {
                        stringResource(id = R.string.rich_text_editor_composer_placeholder)
                    },
                    roundedCorners = roundedCorners,
                    bgColor = bgColor,
                    onError = onError,
                )
            }

            SendButton(
                canSendMessage = canSendMessage,
                onClick = onSendClicked,
                composerMode = composerMode,
                modifier = Modifier
                    .constrainAs(sendButton) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                        visibility = if (!showTextFormatting) Visibility.Visible else Visibility.Gone
                    }
            )
        }

        if (showTextFormatting) {
            TextFormatting(
                state = state,
                onDismiss = onDismissTextFormatting,
                sendButton = {
                    SendButton(
                        canSendMessage = canSendMessage,
                        onClick = onSendClicked,
                        composerMode = composerMode,
                        modifier = it
                    )
                },
            )
        }
    }

    SoftKeyboardEffect(composerMode, onRequestFocus) {
        it is MessageComposerMode.Special
    }

    SoftKeyboardEffect(showTextFormatting, onRequestFocus) { it }
}

@Composable
private fun TextInput(
    state: RichTextEditorState,
    placeholder: String,
    roundedCorners: RoundedCornerShape,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
) {
    val minHeight = 42.dp.applyScaleUp()
    val defaultTypography = ElementTheme.typography.fontBodyLgRegular
    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .background(color = bgColor, shape = roundedCorners)
            .padding(
                PaddingValues(
                    top = 4.dp.applyScaleUp(),
                    bottom = 4.dp.applyScaleUp(),
                    start = 12.dp.applyScaleUp(),
                    end = 42.dp.applyScaleUp()
                )
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

@Composable
private fun TextFormatting(
    state: RichTextEditorState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sendButton: @Composable (modifier: Modifier) -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val (close, formatting, send) = createRefs()

        IconButton(
            modifier = Modifier
                .size(48.dp)
                .constrainAs(close) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            onClick = onDismiss
        ) {
            Icon(
                modifier = Modifier.size(30.dp.applyScaleUp()),
                resourceId = CommonDrawables.ic_cancel,
                contentDescription = stringResource(CommonStrings.action_close),
                tint = ElementTheme.colors.iconPrimary,
            )
        }

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .constrainAs(formatting) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(close.end, margin = 1.dp)
                    end.linkTo(send.start, margin = 14.dp)
                    width = fillToConstraints
                }
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FormattingOption(
                state = state.actions[ComposerAction.BOLD].toButtonState(),
                onClick = { state.toggleInlineFormat(InlineFormat.Bold) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_bold),
                contentDescription = stringResource(R.string.rich_text_editor_format_bold)
            )
            FormattingOption(
                state = state.actions[ComposerAction.ITALIC].toButtonState(),
                onClick = { state.toggleInlineFormat(InlineFormat.Italic) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_italic),
                contentDescription = stringResource(R.string.rich_text_editor_format_italic)
            )
            FormattingOption(
                state = state.actions[ComposerAction.UNDERLINE].toButtonState(),
                onClick = { state.toggleInlineFormat(InlineFormat.Underline) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_underline),
                contentDescription = stringResource(R.string.rich_text_editor_format_underline)
            )
            FormattingOption(
                state = state.actions[ComposerAction.STRIKE_THROUGH].toButtonState(),
                onClick = { state.toggleInlineFormat(InlineFormat.StrikeThrough) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_strikethrough),
                contentDescription = stringResource(R.string.rich_text_editor_format_strikethrough)
            )

            var linkDialogAction by remember { mutableStateOf<LinkAction?>(null) }

            linkDialogAction?.let {
                TextComposerLinkDialog(
                    onDismissRequest = { linkDialogAction = null },
                    onCreateLinkRequest = state::insertLink,
                    onSaveLinkRequest = state::setLink,
                    onRemoveLinkRequest = state::removeLink,
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
                onClick = { state.toggleList(ordered = false) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_bullet_list),
                contentDescription = stringResource(R.string.rich_text_editor_bullet_list)
            )
            FormattingOption(
                state = state.actions[ComposerAction.ORDERED_LIST].toButtonState(),
                onClick = { state.toggleList(ordered = true) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_numbered_list),
                contentDescription = stringResource(R.string.rich_text_editor_numbered_list)
            )
            FormattingOption(
                state = state.actions[ComposerAction.INDENT].toButtonState(),
                onClick = { state.indent() },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_indent_increase),
                contentDescription = stringResource(R.string.rich_text_editor_indent)
            )
            FormattingOption(
                state = state.actions[ComposerAction.UNINDENT].toButtonState(),
                onClick = { state.unindent() },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_indent_decrease),
                contentDescription = stringResource(R.string.rich_text_editor_unindent)
            )
            FormattingOption(
                state = state.actions[ComposerAction.INLINE_CODE].toButtonState(),
                onClick = { state.toggleInlineFormat(InlineFormat.InlineCode) },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_inline_code),
                contentDescription = stringResource(R.string.rich_text_editor_inline_code)
            )
            FormattingOption(
                state = state.actions[ComposerAction.CODE_BLOCK].toButtonState(),
                onClick = { state.toggleCodeBlock() },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_code_block),
                contentDescription = stringResource(R.string.rich_text_editor_code_block)
            )
            FormattingOption(
                state = state.actions[ComposerAction.QUOTE].toButtonState(),
                onClick = { state.toggleQuote() },
                imageVector = ImageVector.vectorResource(CommonDrawables.ic_quote),
                contentDescription = stringResource(R.string.rich_text_editor_quote)
            )
        }

        sendButton(
            Modifier.constrainAs(send) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
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

@DayNightPreviews
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    Column {
        TextComposer(
            RichTextEditorState("", fake = true).apply { requestFocus() },
            canSendMessage = false,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
        TextComposer(
            RichTextEditorState("A message", fake = true).apply { requestFocus() },
            canSendMessage = true,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
        TextComposer(
            RichTextEditorState(
                "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
                fake = true
            ).apply {
                requestFocus()
            },
            canSendMessage = true,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
        TextComposer(
            RichTextEditorState("A message without focus", fake = true),
            canSendMessage = true,
            onSendMessage = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            enableTextFormatting = true,
        )
    }
}

@DayNightPreviews
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    Column {
        TextComposer(
            RichTextEditorState("", fake = true),
            canSendMessage = false,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
        TextComposer(
            RichTextEditorState("A message", fake = true),
            canSendMessage = true,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
        TextComposer(
            RichTextEditorState("A message\nWith several lines\nTo preview larger textfields and long lines with overflow", fake = true),
            canSendMessage = true,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal(""),
            enableTextFormatting = true,
        )
    }
}

@DayNightPreviews
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    TextComposer(
        RichTextEditorState("A message", fake = true).apply { requestFocus() },
        canSendMessage = true,
        onSendMessage = {},
        composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text", TransactionId("1234")),
        onResetComposerMode = {},
        enableTextFormatting = true,
    )
}

@DayNightPreviews
@Composable
internal fun TextComposerReplyPreview() = ElementPreview {
    Column {
        TextComposer(
            RichTextEditorState("", fake = true),
            canSendMessage = false,
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
        TextComposer(
            RichTextEditorState("", fake = true),
            canSendMessage = false,
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
        TextComposer(
            RichTextEditorState("A message", fake = true),
            canSendMessage = true,
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
        TextComposer(
            RichTextEditorState("A message", fake = true),
            canSendMessage = true,
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
        TextComposer(
            RichTextEditorState("A message", fake = true),
            canSendMessage = true,
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
        TextComposer(
            RichTextEditorState("A message", fake = true).apply { requestFocus() },
            canSendMessage = true,
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
    }
}
