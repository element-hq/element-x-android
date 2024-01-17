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

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.media.createFakeWaveform
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.textcomposer.components.ComposerOptionsButton
import io.element.android.libraries.textcomposer.components.DismissTextFormattingButton
import io.element.android.libraries.textcomposer.components.SendButton
import io.element.android.libraries.textcomposer.components.TextFormatting
import io.element.android.libraries.textcomposer.components.VoiceMessageDeleteButton
import io.element.android.libraries.textcomposer.components.VoiceMessagePreview
import io.element.android.libraries.textcomposer.components.VoiceMessageRecorderButton
import io.element.android.libraries.textcomposer.components.VoiceMessageRecording
import io.element.android.libraries.textcomposer.components.textInputRoundedCornerShape
import io.element.android.libraries.textcomposer.mentions.rememberMentionSpanProvider
import io.element.android.libraries.textcomposer.model.Message
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditor
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uniffi.wysiwyg_composer.MenuAction
import kotlin.time.Duration.Companion.seconds

@Composable
fun TextComposer(
    state: RichTextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    enableTextFormatting: Boolean,
    enableVoiceMessages: Boolean,
    currentUserId: UserId,
    onRequestFocus: () -> Unit,
    onSendMessage: (Message) -> Unit,
    onResetComposerMode: () -> Unit,
    onAddAttachment: () -> Unit,
    onDismissTextFormatting: () -> Unit,
    onVoiceRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onVoicePlayerEvent: (VoiceMessagePlayerEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    onDeleteVoiceMessage: () -> Unit,
    onError: (Throwable) -> Unit,
    onSuggestionReceived: (Suggestion?) -> Unit,
    onRichContentSelected: ((Uri) -> Unit)?,
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
    subcomposing: Boolean = false,
) {
    val onSendClicked = {
        val html = if (enableTextFormatting) state.messageHtml else null
        onSendMessage(Message(html = html, markdown = state.messageMarkdown))
    }

    val onPlayVoiceMessageClicked = {
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Play)
    }

    val onPauseVoiceMessageClicked = {
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Pause)
    }

    val onSeekVoiceMessage = { position: Float ->
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Seek(position))
    }

    val layoutModifier = modifier
        .fillMaxSize()
        .height(IntrinsicSize.Min)

    val composerOptionsButton: @Composable () -> Unit = remember {
        @Composable {
            ComposerOptionsButton(
                modifier = Modifier
                    .size(48.dp),
                onClick = onAddAttachment
            )
        }
    }

    val textInput: @Composable () -> Unit = remember(state, subcomposing, composerMode, onResetComposerMode, onError) {
        @Composable {
            val mentionSpanProvider = rememberMentionSpanProvider(currentUserId)
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
                resolveMentionDisplay = { text, url -> TextDisplay.Custom(mentionSpanProvider.getMentionSpanFor(text, url)) },
                resolveRoomMentionDisplay = { TextDisplay.Custom(mentionSpanProvider.getMentionSpanFor("@room", "#")) },
                onError = onError,
                onRichContentSelected = onRichContentSelected,
            )
        }
    }

    val canSendMessage by remember { derivedStateOf { state.messageHtml.isNotEmpty() } }
    val sendButton = @Composable {
        SendButton(
            canSendMessage = canSendMessage,
            onClick = onSendClicked,
            composerMode = composerMode,
        )
    }
    val recordVoiceButton = @Composable {
        VoiceMessageRecorderButton(
            isRecording = voiceMessageState is VoiceMessageState.Recording,
            onEvent = onVoiceRecorderEvent,
        )
    }
    val sendVoiceButton = @Composable {
        SendButton(
            canSendMessage = voiceMessageState is VoiceMessageState.Preview,
            onClick = onSendVoiceMessage,
            composerMode = composerMode,
        )
    }
    val uploadVoiceProgress = @Composable {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
        )
    }

    val textFormattingOptions = @Composable { TextFormatting(state = state) }

    val sendOrRecordButton = when {
        enableVoiceMessages && !canSendMessage ->
            when (voiceMessageState) {
                VoiceMessageState.Idle,
                is VoiceMessageState.Recording -> recordVoiceButton
                is VoiceMessageState.Preview -> when (voiceMessageState.isSending) {
                    true -> uploadVoiceProgress
                    false -> sendVoiceButton
                }
            }
        else ->
            sendButton
    }

    val voiceRecording = @Composable {
        when (voiceMessageState) {
            is VoiceMessageState.Preview ->
                VoiceMessagePreview(
                    isInteractive = !voiceMessageState.isSending,
                    isPlaying = voiceMessageState.isPlaying,
                    showCursor = voiceMessageState.showCursor,
                    waveform = voiceMessageState.waveform,
                    playbackProgress = voiceMessageState.playbackProgress,
                    time = voiceMessageState.time,
                    onPlayClick = onPlayVoiceMessageClicked,
                    onPauseClick = onPauseVoiceMessageClicked,
                    onSeek = onSeekVoiceMessage,
                )
            is VoiceMessageState.Recording ->
                VoiceMessageRecording(voiceMessageState.levels, voiceMessageState.duration)
            VoiceMessageState.Idle -> {}
        }
    }

    val voiceDeleteButton = @Composable {
        when (voiceMessageState) {
            is VoiceMessageState.Preview ->
                VoiceMessageDeleteButton(enabled = !voiceMessageState.isSending, onClick = onDeleteVoiceMessage)
            is VoiceMessageState.Recording ->
                VoiceMessageDeleteButton(enabled = true, onClick = { onVoiceRecorderEvent(VoiceMessageRecorderEvent.Cancel) })
            else -> {}
        }
    }

    if (showTextFormatting) {
        TextFormattingLayout(
            modifier = layoutModifier,
            textInput = textInput,
            dismissTextFormattingButton = {
                DismissTextFormattingButton(onClick = onDismissTextFormatting)
            },
            textFormatting = textFormattingOptions,
            sendButton = sendButton,
        )
    } else {
        StandardLayout(
            voiceMessageState = voiceMessageState,
            enableVoiceMessages = enableVoiceMessages,
            modifier = layoutModifier,
            composerOptionsButton = composerOptionsButton,
            textInput = textInput,
            endButton = sendOrRecordButton,
            voiceRecording = voiceRecording,
            voiceDeleteButton = voiceDeleteButton,
        )
    }

    if (!subcomposing) {
        SoftKeyboardEffect(composerMode, onRequestFocus) {
            it is MessageComposerMode.Special
        }

        SoftKeyboardEffect(showTextFormatting, onRequestFocus) { it }
    }

    val menuAction = state.menuAction
    LaunchedEffect(menuAction) {
        if (menuAction is MenuAction.Suggestion) {
            val suggestion = Suggestion(menuAction.suggestionPattern)
            onSuggestionReceived(suggestion)
        } else {
            onSuggestionReceived(null)
        }
    }
}

@Composable
private fun StandardLayout(
    voiceMessageState: VoiceMessageState,
    enableVoiceMessages: Boolean,
    textInput: @Composable () -> Unit,
    composerOptionsButton: @Composable () -> Unit,
    voiceRecording: @Composable () -> Unit,
    voiceDeleteButton: @Composable () -> Unit,
    endButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (enableVoiceMessages && voiceMessageState !is VoiceMessageState.Idle) {
            if (voiceMessageState is VoiceMessageState.Preview || voiceMessageState is VoiceMessageState.Recording) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 5.dp, top = 5.dp, end = 3.dp, start = 3.dp)
                        .size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    voiceDeleteButton()
                }
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 8.dp)
                    .weight(1f)
            ) {
                voiceRecording()
            }
        } else {
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
        }
        Box(
            Modifier
                .padding(bottom = 5.dp, top = 5.dp, end = 6.dp, start = 6.dp)
                .size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            endButton()
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
    resolveRoomMentionDisplay: () -> TextDisplay,
    resolveMentionDisplay: (text: String, url: String) -> TextDisplay,
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    onRichContentSelected: ((Uri) -> Unit)? = null,
) {
    val bgColor = ElementTheme.colors.bgSubtleSecondary
    val borderColor = ElementTheme.colors.borderDisabled
    val roundedCorners = textInputRoundedCornerShape(composerMode = composerMode)

    Column(
        modifier = modifier
            .clip(roundedCorners)
            .border(0.5.dp, borderColor, roundedCorners)
            .background(color = bgColor)
            .requiredHeightIn(min = 42.dp)
            .fillMaxSize(),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(composerMode = composerMode, onResetComposerMode = onResetComposerMode)
        }
        val defaultTypography = ElementTheme.typography.fontBodyLgRegular
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 42.dp)
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
                // Disable most of the editor functionality if it's just being measured for a subcomposition.
                // This prevents it gaining focus and mutating the state.
                registerStateUpdates = !subcomposing,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.hasFocus),
                resolveMentionDisplay = resolveMentionDisplay,
                resolveRoomMentionDisplay = resolveRoomMentionDisplay,
                onError = onError,
                onRichContentSelected = onRichContentSelected,
            )
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
            imageVector = CompoundIcons.Edit,
            contentDescription = stringResource(CommonStrings.common_editing),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(16.dp),
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
            imageVector = CompoundIcons.Close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = ElementTheme.materialColors.secondary,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 12.dp)
                .size(16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds(),
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
            imageVector = CompoundIcons.Close,
            contentDescription = stringResource(CommonStrings.action_close),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(end = 4.dp, top = 4.dp, start = 16.dp, bottom = 16.dp)
                .size(16.dp)
                .clickable(
                    enabled = true,
                    onClick = onResetComposerMode,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    PreviewColumn(
        items = persistentListOf(
            {
                ATextComposer(
                    RichTextEditorState("", initialFocus = true),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Normal,
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost"),
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message", initialFocus = true),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Normal,
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState(
                        "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
                        initialFocus = true
                    ),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Normal,
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message without focus", initialFocus = false),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Normal,
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            }
        )
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        ATextComposer(
            RichTextEditorState("", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
            enableTextFormatting = true,
            enableVoiceMessages = true,
            currentUserId = UserId("@alice:localhost")
        )
    }, {
        ATextComposer(
            RichTextEditorState("A message", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
            enableTextFormatting = true,
            enableVoiceMessages = true,
            currentUserId = UserId("@alice:localhost")
        )
    }, {
        ATextComposer(
            RichTextEditorState("A message\nWith several lines\nTo preview larger textfields and long lines with overflow", initialFocus = false),
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
            enableTextFormatting = true,
            enableVoiceMessages = true,
            currentUserId = UserId("@alice:localhost")
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    PreviewColumn(items = persistentListOf({
        ATextComposer(
            RichTextEditorState("A message", initialFocus = true),
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Edit(EventId("$1234"), "Some text", TransactionId("1234")),
            enableTextFormatting = true,
            enableVoiceMessages = true,
            currentUserId = UserId("@alice:localhost")
        )
    }))
}

@PreviewsDayNight
@Composable
internal fun TextComposerReplyPreview() = ElementPreview {
    PreviewColumn(
        items = persistentListOf(
            {
                ATextComposer(
                    RichTextEditorState(""),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Reply(
                        isThreaded = false,
                        senderName = "Alice",
                        eventId = EventId("$1234"),
                        attachmentThumbnailInfo = null,
                        defaultContent = "A message\n" +
                            "With several lines\n" +
                            "To preview larger textfields and long lines with overflow"
                    ),
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState(""),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Reply(
                        isThreaded = true,
                        senderName = "Alice",
                        eventId = EventId("$1234"),
                        attachmentThumbnailInfo = null,
                        defaultContent = "A message\n" +
                            "With several lines\n" +
                            "To preview larger textfields and long lines with overflow"
                    ),
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message"),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Reply(
                        isThreaded = true,
                        senderName = "Alice",
                        eventId = EventId("$1234"),
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = MediaSource("https://domain.com/image.jpg"),
                            textContent = "image.jpg",
                            type = AttachmentThumbnailType.Image,
                            blurHash = A_BLUR_HASH,
                        ),
                        defaultContent = "image.jpg"
                    ),
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message"),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Reply(
                        isThreaded = false,
                        senderName = "Alice",
                        eventId = EventId("$1234"),
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = MediaSource("https://domain.com/video.mp4"),
                            textContent = "video.mp4",
                            type = AttachmentThumbnailType.Video,
                            blurHash = A_BLUR_HASH,
                        ),
                        defaultContent = "video.mp4"
                    ),
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message"),
                    voiceMessageState = VoiceMessageState.Idle,
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
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            },
            {
                ATextComposer(
                    RichTextEditorState("A message", initialFocus = true),
                    voiceMessageState = VoiceMessageState.Idle,
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
                    enableTextFormatting = true,
                    enableVoiceMessages = true,
                    currentUserId = UserId("@alice:localhost")
                )
            }
        )
    )
}

@PreviewsDayNight
@Composable
internal fun TextComposerVoicePreview() = ElementPreview {
    @Composable
    fun VoicePreview(
        voiceMessageState: VoiceMessageState
    ) = ATextComposer(
        RichTextEditorState("", initialFocus = true),
        voiceMessageState = voiceMessageState,
        composerMode = MessageComposerMode.Normal,
        enableTextFormatting = true,
        enableVoiceMessages = true,
        currentUserId = UserId("@alice:localhost")
    )
    PreviewColumn(items = persistentListOf({
        VoicePreview(voiceMessageState = VoiceMessageState.Recording(61.seconds, createFakeWaveform()))
    }, {
        VoicePreview(
            voiceMessageState = VoiceMessageState.Preview(
                isSending = false,
                isPlaying = false,
                showCursor = false,
                waveform = createFakeWaveform(),
                time = 0.seconds,
                playbackProgress = 0.0f
            )
        )
    }, {
        VoicePreview(
            voiceMessageState = VoiceMessageState.Preview(
                isSending = false,
                isPlaying = true,
                showCursor = true,
                waveform = createFakeWaveform(),
                time = 3.seconds,
                playbackProgress = 0.2f
            )
        )
    }, {
        VoicePreview(
            voiceMessageState = VoiceMessageState.Preview(
                isSending = true,
                isPlaying = false,
                showCursor = false,
                waveform = createFakeWaveform(),
                time = 61.seconds,
                playbackProgress = 0.0f
            )
        )
    }))
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

@Composable
private fun ATextComposer(
    richTextEditorState: RichTextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    enableTextFormatting: Boolean,
    enableVoiceMessages: Boolean,
    currentUserId: UserId,
    showTextFormatting: Boolean = false,
) {
    TextComposer(
        state = richTextEditorState,
        showTextFormatting = showTextFormatting,
        voiceMessageState = voiceMessageState,
        composerMode = composerMode,
        enableTextFormatting = enableTextFormatting,
        enableVoiceMessages = enableVoiceMessages,
        currentUserId = currentUserId,
        onRequestFocus = {},
        onSendMessage = {},
        onResetComposerMode = {},
        onAddAttachment = {},
        onDismissTextFormatting = {},
        onVoiceRecorderEvent = {},
        onVoicePlayerEvent = {},
        onSendVoiceMessage = {},
        onDeleteVoiceMessage = {},
        onError = {},
        onSuggestionReceived = {},
        onRichContentSelected = null,
    )
}
