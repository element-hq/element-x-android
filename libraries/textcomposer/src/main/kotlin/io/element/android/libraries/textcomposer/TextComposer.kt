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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.VectorIcons
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposer(
    composerText: String?,
    composerMode: MessageComposerMode,
    composerCanSendMessage: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
    onSendMessage: (String) -> Unit = {},
    onResetComposerMode: () -> Unit = {},
    onComposerTextChange: (CharSequence) -> Unit = {},
    onAddAttachment:() -> Unit = {},
) {
    val text = composerText.orEmpty()
    Row(modifier.padding(
        horizontal = 12.dp,
        vertical = 8.dp
    ), verticalAlignment = Alignment.Bottom) {
        AttachmentButton(onClick = onAddAttachment, modifier = Modifier.padding(vertical = 6.dp))
        Spacer(modifier = Modifier.width(12.dp))
        var lineCount by remember { mutableStateOf(0) }
        val roundedCorners = remember(lineCount, composerMode) {
            if (lineCount > 1 || composerMode is MessageComposerMode.Special) {
                RoundedCornerShape(20.dp)
            } else {
                RoundedCornerShape(28.dp)
            }
        }

        val minHeight = 42.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(roundedCorners)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, roundedCorners)
        ) {
            if (composerMode is MessageComposerMode.Special) {
                ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
            }
            val defaultTypography = ElementTextStyles.Regular.callout.copy(textAlign = TextAlign.Start)
            Box {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .focusRequester(focusRequester),
                    value = text,
                    onValueChange = { onComposerTextChange(it) },
                    onTextLayout = {
                        lineCount = it.lineCount
                    },
                    textStyle = defaultTypography.copy(color = MaterialTheme.colorScheme.primary),
                    cursorBrush = SolidColor(LocalColors.current.accentColor),
                    decorationBox = { innerTextField ->
                        TextFieldDefaults.DecorationBox(
                            value = text,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = false,
                            visualTransformation = VisualTransformation.None,
                            shape = roundedCorners,
                            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 42.dp),
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(stringResource(StringR.string.common_message), style = defaultTypography)
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    }
                )

                SendButton(
                    text = text,
                    canSendMessage = composerCanSendMessage,
                    onSendMessage = onSendMessage,
                    composerMode = composerMode,
                    modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                )
            }
        }
    }
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
                text = composerMode.defaultContent.toString(),
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
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Icon(
            resourceId = VectorIcons.Edit,
            contentDescription = stringResource(R.string.editing),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            stringResource(R.string.editing),
            style = ElementTextStyles.Regular.caption2,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(StringR.string.action_close),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(16.dp)
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = MutableInteractionSource(),
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
    val paddings = if (attachmentThumbnailInfo != null) {
        PaddingValues(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
    } else {
        PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
    }
    Row(
        modifier
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(paddings)
    ) {
        if (attachmentThumbnailInfo != null) {
            AttachmentThumbnail(
                info = attachmentThumbnailInfo,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(verticalArrangement = Arrangement.SpaceEvenly) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    senderName,
                    style = ElementTextStyles.Regular.caption2.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(StringR.string.action_close),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            enabled = true,
                            onClick = onResetComposerMode,
                            interactionSource = MutableInteractionSource(),
                            indication = rememberRipple(bounded = false)
                        ),
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text.orEmpty(),
                style = ElementTextStyles.Regular.caption1,
                textAlign = TextAlign.Start,
                color = LocalColors.current.placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AttachmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Surface(
            Modifier
                .size(30.dp)
                .clickable(true, onClick = onClick),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Image(
                modifier = Modifier.size(12.5f.dp),
                painter = painterResource(R.drawable.ic_add_attachment),
                contentDescription = null,
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(
                    LocalContentColor.current
                )
            )
        }
    }
}

@Composable
private fun BoxScope.SendButton(
    text: String,
    canSendMessage: Boolean,
    onSendMessage: (String) -> Unit,
    composerMode: MessageComposerMode,
    modifier: Modifier = Modifier,
) {
    val interactionSource = MutableInteractionSource()
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (canSendMessage) LocalColors.current.accentColor else Color.Transparent)
            .size(30.dp)
            .align(Alignment.BottomEnd)
            .applyIf(composerMode !is MessageComposerMode.Edit, ifTrue = {
                padding(start = 1.dp) // Center the arrow in the circle
            })
            .clickable(
                enabled = canSendMessage,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false),
                onClick = {
                    onSendMessage(text)
                }),
        contentAlignment = Alignment.Center,
    ) {
        val iconId = when (composerMode) {
            is MessageComposerMode.Edit -> R.drawable.ic_tick
            else -> R.drawable.ic_send
        }
        val contentDescription = when (composerMode) {
            is MessageComposerMode.Edit -> stringResource(StringR.string.action_edit)
            else -> stringResource(StringR.string.action_send)
        }
        Icon(
            modifier = Modifier.size(16.dp),
            resourceId = iconId,
            contentDescription = contentDescription,
            tint = if (canSendMessage) Color.White else LocalColors.current.quaternary
        )
    }
}

@Preview
@Composable
internal fun TextComposerSimpleLightPreview() = ElementPreviewLight { SimpleContentToPreview() }

@Preview
@Composable
internal fun TextComposerSimpleDarkPreview() = ElementPreviewDark { SimpleContentToPreview() }

@Preview
@Composable
internal fun TextComposerEditLightPreview() = ElementPreviewLight { EditContentToPreview() }

@Preview
@Composable
internal fun TextComposerEditDarkPreview() = ElementPreviewDark { EditContentToPreview() }

@Preview
@Composable
internal fun TextComposerReplyLightPreview() = ElementPreviewLight { ReplyContentToPreview() }

@Preview
@Composable
internal fun TextComposerReplyDarkPreview() = ElementPreviewDark { ReplyContentToPreview() }

@Composable
private fun SimpleContentToPreview() {
    Column {
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            composerCanSendMessage = false,
            composerText = "",
        )
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message",
        )
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Normal(""),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
        )
    }
}

@Composable
private fun EditContentToPreview() {
    TextComposer(
        onSendMessage = {},
        onComposerTextChange = {},
        composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text"),
        onResetComposerMode = {},
        composerCanSendMessage = true,
        composerText = "A message",
    )
}

@Composable
private fun ReplyContentToPreview() {
    Column {
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Reply(
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = null,
                defaultContent = "A message\n" +
                    "With several lines\n" +
                    "To preview larger textfields and long lines with overflow"
            ),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message",
        )
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Reply(
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    mediaSource = MediaSource("https://domain.com/image.jpg"),
                    textContent = "image.jpg",
                    type = AttachmentThumbnailType.Image,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "image.jpg"
            ),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message",
        )
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Reply(
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    mediaSource = MediaSource("https://domain.com/video.mp4"),
                    textContent = "video.mp4",
                    type = AttachmentThumbnailType.Video,
                    blurHash = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr",
                ),
                defaultContent = "video.mp4"
            ),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message",
        )
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Reply(
                senderName = "Alice",
                eventId = EventId("$1234"),
                attachmentThumbnailInfo = AttachmentThumbnailInfo(
                    mediaSource = null,
                    textContent = "logs.txt",
                    type = AttachmentThumbnailType.File,
                    blurHash = null,
                ),
                defaultContent = "logs.txt"
            ),
            onResetComposerMode = {},
            composerCanSendMessage = true,
            composerText = "A message",
        )
    }
}
