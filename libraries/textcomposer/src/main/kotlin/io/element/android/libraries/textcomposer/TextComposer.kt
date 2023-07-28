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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.VectorIcons
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TextComposer(
    composerText: String?,
    composerMode: MessageComposerMode,
    composerCanSendMessage: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
    onSendMessage: (String) -> Unit = {},
    onResetComposerMode: () -> Unit = {},
    onComposerTextChange: (String) -> Unit = {},
    onAddAttachment: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val text = composerText.orEmpty()
    Row(
        modifier.padding(
            horizontal = 12.dp,
            vertical = 8.dp
        ), verticalAlignment = Alignment.Bottom
    ) {
        AttachmentButton(onClick = onAddAttachment, modifier = Modifier.padding(vertical = 6.dp))
        Spacer(modifier = Modifier.width(12.dp))
        val roundCornerSmall = 20.dp.applyScaleUp()
        val roundCornerLarge = 28.dp.applyScaleUp()

        var lineCount by remember { mutableStateOf(0) }
        val roundedCornerSize = remember(lineCount, composerMode) {
            if (lineCount > 1 || composerMode is MessageComposerMode.Special) {
                roundCornerSmall
            } else {
                roundCornerLarge
            }
        }
        val roundedCornerSizeState = animateDpAsState(
            targetValue = roundedCornerSize,
            animationSpec = tween(
                durationMillis = 100,
            )
        )
        val roundedCorners = RoundedCornerShape(roundedCornerSizeState.value)
        val minHeight = 42.dp.applyScaleUp()
        val bgColor = ElementTheme.colors.bgSubtleSecondary
        // Change border color depending on focus
        var hasFocus by remember { mutableStateOf(false) }
        val borderColor = if (hasFocus) ElementTheme.colors.borderDisabled else bgColor
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(roundedCorners)
                .background(color = bgColor)
                .border(1.dp, borderColor, roundedCorners)
        ) {
            if (composerMode is MessageComposerMode.Special) {
                ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
            }
            val defaultTypography = ElementTheme.typography.fontBodyLgRegular
            Box {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .focusRequester(focusRequester)
                        .onFocusEvent {
                            hasFocus = it.hasFocus
                            onFocusChanged(it.hasFocus)
                        },
                    value = text,
                    onValueChange = { onComposerTextChange(it) },
                    onTextLayout = {
                        lineCount = it.lineCount
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    textStyle = defaultTypography.copy(color = MaterialTheme.colorScheme.primary),
                    cursorBrush = SolidColor(ElementTheme.colors.iconAccentTertiary),
                    decorationBox = { innerTextField ->
                        TextFieldDefaults.DecorationBox(
                            value = text,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = false,
                            visualTransformation = VisualTransformation.None,
                            shape = roundedCorners,
                            contentPadding = PaddingValues(
                                top = 10.dp.applyScaleUp(),
                                bottom = 10.dp.applyScaleUp(),
                                start = 12.dp.applyScaleUp(),
                                end = 42.dp.applyScaleUp(),
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(stringResource(CommonStrings.common_message), style = defaultTypography)
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                                focusedTextColor = MaterialTheme.colorScheme.primary,
                                unfocusedPlaceholderColor = ElementTheme.colors.textDisabled,
                                focusedPlaceholderColor = ElementTheme.colors.textDisabled,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = bgColor,
                                focusedContainerColor = bgColor,
                                errorContainerColor = bgColor,
                                disabledContainerColor = bgColor,
                            )
                        )
                    }
                )

                SendButton(
                    text = text,
                    canSendMessage = composerCanSendMessage,
                    onSendMessage = onSendMessage,
                    composerMode = composerMode,
                    modifier = Modifier.padding(end = 6.dp.applyScaleUp(), bottom = 6.dp.applyScaleUp())
                )
            }
        }
    }

    // Request focus when changing mode, and show keyboard.
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(composerMode) {
        if (composerMode is MessageComposerMode.Special) {
            focusRequester.requestFocus()
            keyboard?.let {
                awaitFrame()
                it.show()
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        Icon(
            resourceId = VectorIcons.Edit,
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
            imageVector = Icons.Default.Close,
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
            imageVector = Icons.Default.Close,
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
private fun AttachmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier
            .size(30.dp.applyScaleUp())
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = ElementTheme.colors.iconPrimary
    ) {
        Image(
            modifier = Modifier.size(12.5f.dp.applyScaleUp()),
            painter = painterResource(R.drawable.ic_add_attachment),
            contentDescription = stringResource(R.string.rich_text_editor_a11y_add_attachment),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(
                LocalContentColor.current
            )
        )
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
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (canSendMessage) ElementTheme.colors.iconAccentTertiary else Color.Transparent)
            .size(30.dp.applyScaleUp())
            .align(Alignment.BottomEnd)
            .applyIf(composerMode !is MessageComposerMode.Edit, ifTrue = {
                padding(start = 1.dp.applyScaleUp()) // Center the arrow in the circle
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
            is MessageComposerMode.Edit -> stringResource(CommonStrings.action_edit)
            else -> stringResource(CommonStrings.action_send)
        }
        Icon(
            modifier = Modifier.size(16.dp.applyScaleUp()),
            resourceId = iconId,
            contentDescription = contentDescription,
            // Exception here, we use Color.White instead of ElementTheme.colors.iconOnSolidPrimary
            tint = if (canSendMessage) Color.White else ElementTheme.colors.iconDisabled
        )
    }
}

@DayNightPreviews
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
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

@DayNightPreviews
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    TextComposer(
        onSendMessage = {},
        onComposerTextChange = {},
        composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text", TransactionId("1234")),
        onResetComposerMode = {},
        composerCanSendMessage = true,
        composerText = "A message",
    )
}

@DayNightPreviews
@Composable
internal fun TextComposerReplyPreview() = ElementPreview {
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
                    thumbnailSource = MediaSource("https://domain.com/image.jpg"),
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
                    thumbnailSource = MediaSource("https://domain.com/video.mp4"),
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
                    thumbnailSource = null,
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
        TextComposer(
            onSendMessage = {},
            onComposerTextChange = {},
            composerMode = MessageComposerMode.Reply(
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
            composerCanSendMessage = true,
            composerText = "A message",
        )
    }
}
