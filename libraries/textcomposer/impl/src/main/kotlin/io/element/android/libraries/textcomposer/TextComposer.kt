/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.androidutils.ui.showKeyboard
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.designsystem.preview.DAY_MODE_NAME
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.NIGHT_MODE_NAME
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconColorButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetailsProvider
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.textcomposer.components.SendButtonIcon
import io.element.android.libraries.textcomposer.components.TextFormatting
import io.element.android.libraries.textcomposer.components.VoiceMessageDeleteButtonIcon
import io.element.android.libraries.textcomposer.components.VoiceMessagePreview
import io.element.android.libraries.textcomposer.components.VoiceMessageRecorderButtonIcon
import io.element.android.libraries.textcomposer.components.VoiceMessageRecording
import io.element.android.libraries.textcomposer.components.markdown.MarkdownTextInput
import io.element.android.libraries.textcomposer.components.textInputRoundedCornerShape
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown
import io.element.android.libraries.textcomposer.model.aTextEditorStateRich
import io.element.android.libraries.textcomposer.model.showCaptionCompatibilityWarning
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.compose.RichTextEditor
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import uniffi.wysiwyg_composer.MenuAction
import kotlin.time.Duration.Companion.seconds

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2012-39036
 */
@Composable
fun TextComposer(
    state: TextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    onRequestFocus: () -> Unit,
    onSendMessage: () -> Unit,
    onResetComposerMode: () -> Unit,
    onAddAttachment: () -> Unit,
    onDismissTextFormatting: () -> Unit,
    onVoiceRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onVoicePlayerEvent: (VoiceMessagePlayerEvent) -> Unit,
    onSendVoiceMessage: () -> Unit,
    onDeleteVoiceMessage: () -> Unit,
    onError: (Throwable) -> Unit,
    onTyping: (Boolean) -> Unit,
    onReceiveSuggestion: (Suggestion?) -> Unit,
    onSelectRichContent: ((Uri) -> Unit)?,
    resolveMentionDisplay: (text: String, url: String) -> TextDisplay,
    resolveAtRoomMentionDisplay: () -> TextDisplay,
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
) {
    val markdown = when (state) {
        is TextEditorState.Markdown -> state.state.text.value()
        is TextEditorState.Rich -> state.richTextEditorState.messageMarkdown
    }

    val onPlayVoiceMessageClick = {
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Play)
    }

    val onPauseVoiceMessageClick = {
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Pause)
    }

    val onSeekVoiceMessage = { position: Float ->
        onVoicePlayerEvent(VoiceMessagePlayerEvent.Seek(position))
    }

    val layoutModifier = modifier
        .fillMaxSize()
        .height(IntrinsicSize.Min)

    val placeholder = if (composerMode.inThread) {
        stringResource(id = CommonStrings.action_reply_in_thread)
    } else if (composerMode is MessageComposerMode.Attachment || composerMode is MessageComposerMode.EditCaption) {
        stringResource(id = R.string.rich_text_editor_composer_caption_placeholder)
    } else {
        stringResource(id = R.string.rich_text_editor_composer_placeholder)
    }
    val textInput: @Composable () -> Unit = when (state) {
        is TextEditorState.Rich -> {
            val coroutineScope = rememberCoroutineScope()
            val view = LocalView.current
            remember(state.richTextEditorState, composerMode, onResetComposerMode, onError) {
                @Composable {
                    TextInputBox(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                coroutineScope.launch {
                                    state.requestFocus()
                                    view.showKeyboard()
                                }
                            }
                            .semantics {
                                hideFromAccessibility()
                            },
                        composerMode = composerMode,
                        onResetComposerMode = onResetComposerMode,
                        isTextEmpty = state.richTextEditorState.messageHtml.isEmpty(),
                    ) {
                        RichTextEditor(
                            state = state.richTextEditorState,
                            placeholder = placeholder,
                            registerStateUpdates = true,
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 6.dp)
                                .fillMaxWidth(),
                            style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.richTextEditorState.hasFocus),
                            resolveMentionDisplay = resolveMentionDisplay,
                            resolveRoomMentionDisplay = resolveAtRoomMentionDisplay,
                            onError = onError,
                            onRichContentSelected = onSelectRichContent,
                            onTyping = onTyping,
                        )
                    }
                }
            }
        }
        is TextEditorState.Markdown -> {
            @Composable {
                val style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.hasFocus())
                TextInputBox(
                    composerMode = composerMode,
                    onResetComposerMode = onResetComposerMode,
                    isTextEmpty = state.state.text.value().isEmpty(),
                ) {
                    MarkdownTextInput(
                        state = state.state,
                        placeholder = placeholder,
                        placeholderColor = ElementTheme.colors.textSecondary,
                        onTyping = onTyping,
                        onReceiveSuggestion = onReceiveSuggestion,
                        richTextEditorStyle = style,
                        onSelectRichContent = onSelectRichContent,
                    )
                }
            }
        }
    }

    val canSendTextMessage = markdown.isNotBlank() || composerMode is MessageComposerMode.Attachment

    val textFormattingOptions: @Composable (() -> Unit)? = (state as? TextEditorState.Rich)?.let {
        @Composable { TextFormatting(state = it.richTextEditorState) }
    }

    val hapticFeedback = LocalHapticFeedback.current

    fun performHapticFeedback() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    @Composable
    fun rememberEndButtonParams() = remember(
        composerMode.isEditing,
        voiceMessageState.endButtonKey(),
        canSendTextMessage,
    ) {
        when {
            !canSendTextMessage ->
                when (voiceMessageState) {
                    VoiceMessageState.Idle -> EndButtonParams(
                        endButtonContentDescriptionResId = CommonStrings.a11y_voice_message_record,
                        endButtonClick = {
                            performHapticFeedback()
                            onVoiceRecorderEvent.invoke(VoiceMessageRecorderEvent.Start)
                        },
                        endButtonContent = @Composable {
                            VoiceMessageRecorderButtonIcon(
                                isRecording = false,
                            )
                        }
                    )
                    is VoiceMessageState.Recording -> EndButtonParams(
                        endButtonContentDescriptionResId = CommonStrings.a11y_voice_message_stop_recording,
                        endButtonClick = {
                            performHapticFeedback()
                            onVoiceRecorderEvent.invoke(VoiceMessageRecorderEvent.Stop)
                        },
                        endButtonContent = @Composable {
                            VoiceMessageRecorderButtonIcon(
                                isRecording = true,
                            )
                        }
                    )
                    is VoiceMessageState.Preview -> if (voiceMessageState.isSending) {
                        EndButtonParams(
                            endButtonContentDescriptionResId = CommonStrings.common_sending,
                            endButtonClick = {},
                            endButtonContent = @Composable {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        )
                    } else {
                        EndButtonParams(
                            endButtonContentDescriptionResId = CommonStrings.action_send_voice_message,
                            endButtonClick = {
                                onSendVoiceMessage()
                            },
                            endButtonContent = @Composable {
                                SendButtonIcon(
                                    canSendMessage = true,
                                    isEditing = composerMode.isEditing,
                                )
                            },
                        )
                    }
                }
            composerMode.isEditing -> EndButtonParams(
                endButtonContentDescriptionResId = CommonStrings.action_send_edited_message,
                endButtonClick = {
                    onSendMessage()
                },
                endButtonContent = @Composable {
                    SendButtonIcon(
                        canSendMessage = true,
                        isEditing = true,
                    )
                },
            )
            else -> EndButtonParams(
                endButtonContentDescriptionResId = CommonStrings.action_send_message,
                endButtonClick = {
                    onSendMessage()
                },
                endButtonContent = @Composable {
                    SendButtonIcon(
                        canSendMessage = true,
                        isEditing = false,
                    )
                },
            )
        }
    }

    @Composable
    fun rememberEndButtonParamsFormatting() = remember(composerMode.isEditing, canSendTextMessage) {
        if (composerMode.isEditing) {
            EndButtonParams(
                endButtonContentDescriptionResId = CommonStrings.action_send_edited_message,
                endButtonClick = {
                    if (canSendTextMessage) {
                        onSendMessage()
                    }
                },
                endButtonContent = @Composable {
                    SendButtonIcon(
                        canSendMessage = canSendTextMessage,
                        isEditing = true,
                    )
                },
            )
        } else {
            EndButtonParams(
                endButtonContentDescriptionResId = CommonStrings.action_send_message,
                endButtonClick = {
                    if (canSendTextMessage) {
                        onSendMessage()
                    }
                },
                endButtonContent = @Composable {
                    SendButtonIcon(
                        canSendMessage = canSendTextMessage,
                        isEditing = false,
                    )
                },
            )
        }
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
                    onPlayClick = onPlayVoiceMessageClick,
                    onPauseClick = onPauseVoiceMessageClick,
                    onSeek = onSeekVoiceMessage,
                )
            is VoiceMessageState.Recording ->
                VoiceMessageRecording(
                    levels = voiceMessageState.levels,
                    duration = voiceMessageState.duration,
                )
            VoiceMessageState.Idle -> {}
        }
    }

    if (showTextFormatting && textFormattingOptions != null) {
        val endButtonParams = rememberEndButtonParamsFormatting()
        TextFormattingLayout(
            modifier = layoutModifier,
            isRoomEncrypted = state.isRoomEncrypted,
            textInput = textInput,
            dismissTextFormattingButton = {
                IconColorButton(
                    onClick = onDismissTextFormatting,
                    imageVector = CompoundIcons.Close(),
                    contentDescription = stringResource(R.string.rich_text_editor_close_formatting_options),
                )
            },
            textFormatting = textFormattingOptions,
            endButtonParams = endButtonParams,
        )
    } else {
        val endButtonParams = rememberEndButtonParams()
        StandardLayout(
            composerMode = composerMode,
            voiceMessageState = voiceMessageState,
            isRoomEncrypted = state.isRoomEncrypted,
            modifier = layoutModifier,
            textInput = textInput,
            endButtonParams = endButtonParams,
            voiceRecording = voiceRecording,
            onAddAttachment = onAddAttachment,
            onDeleteVoiceMessage = onDeleteVoiceMessage,
            onVoiceRecorderEvent = onVoiceRecorderEvent,
            onResetComposerMode = onResetComposerMode,
        )
    }

    SoftKeyboardEffect(composerMode, onRequestFocus) {
        it is MessageComposerMode.Special
    }

    SoftKeyboardEffect(showTextFormatting, onRequestFocus) { it }

    // Re-focus the text input when voice recording ends so the user can continue typing
    var previousVoiceMessageState by remember { mutableStateOf(voiceMessageState) }
    LaunchedEffect(voiceMessageState) {
        if (voiceMessageState is VoiceMessageState.Idle && previousVoiceMessageState !is VoiceMessageState.Idle) {
            onRequestFocus()
        }
        previousVoiceMessageState = voiceMessageState
    }

    val latestOnReceiveSuggestion by rememberUpdatedState(onReceiveSuggestion)
    if (state is TextEditorState.Rich) {
        val menuAction = state.richTextEditorState.menuAction
        LaunchedEffect(menuAction) {
            if (menuAction is MenuAction.Suggestion) {
                val suggestion = Suggestion(menuAction.suggestionPattern)
                latestOnReceiveSuggestion(suggestion)
            } else {
                latestOnReceiveSuggestion(null)
            }
        }
    }
}

private data class EndButtonParams(
    val endButtonContentDescriptionResId: Int,
    val endButtonClick: () -> Unit,
    val endButtonContent: @Composable () -> Unit,
)

@Composable
private fun StandardLayout(
    composerMode: MessageComposerMode,
    voiceMessageState: VoiceMessageState,
    isRoomEncrypted: Boolean?,
    textInput: @Composable () -> Unit,
    voiceRecording: @Composable () -> Unit,
    endButtonParams: EndButtonParams,
    onAddAttachment: () -> Unit,
    onDeleteVoiceMessage: () -> Unit,
    onVoiceRecorderEvent: (VoiceMessageRecorderEvent) -> Unit,
    onResetComposerMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isRoomEncrypted == false) {
            Spacer(Modifier.height(16.dp))
            NotEncryptedBadge()
            Spacer(Modifier.height(4.dp))
        }
        Row(verticalAlignment = Alignment.Bottom) {
            when (composerMode) {
                is MessageComposerMode.Attachment -> {
                    Spacer(modifier = Modifier.width(12.dp))
                }
                is MessageComposerMode.EditCaption -> {
                    Spacer(modifier = Modifier.width(19.dp))
                }
                else -> {
                    val endPadding = if (voiceMessageState is VoiceMessageState.Idle) 0.dp else 3.dp
                    // To avoid loosing keyboard focus, the IconButton has to be defined here and has to be always enabled.
                    IconButton(
                        modifier = Modifier
                            .padding(top = 5.dp, bottom = 5.dp, start = 3.dp, end = endPadding)
                            .size(48.dp),
                        onClick = {
                            if (voiceMessageState is VoiceMessageState.Idle) {
                                onAddAttachment()
                            } else {
                                when (voiceMessageState) {
                                    is VoiceMessageState.Preview -> if (!voiceMessageState.isSending) {
                                        onDeleteVoiceMessage()
                                    }
                                    is VoiceMessageState.Recording ->
                                        onVoiceRecorderEvent(VoiceMessageRecorderEvent.Cancel)
                                }
                            }
                        },
                    ) {
                        if (voiceMessageState is VoiceMessageState.Idle) {
                            Icon(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(30.dp)
                                    .background(ElementTheme.colors.iconPrimary)
                                    .padding(3.dp),
                                imageVector = CompoundIcons.Plus(),
                                contentDescription = stringResource(R.string.rich_text_editor_a11y_add_attachment),
                                tint = ElementTheme.colors.iconOnSolidPrimary
                            )
                        } else {
                            when (voiceMessageState) {
                                is VoiceMessageState.Preview ->
                                    VoiceMessageDeleteButtonIcon(enabled = !voiceMessageState.isSending)
                                is VoiceMessageState.Recording ->
                                    VoiceMessageDeleteButtonIcon(enabled = true)
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 8.dp)
                    .weight(1f)
            ) {
                if (voiceMessageState is VoiceMessageState.Idle) {
                    textInput()
                } else if (composerMode is MessageComposerMode.Special) {
                    TextInputBox(
                        composerMode = composerMode,
                        onResetComposerMode = onResetComposerMode,
                        isTextEmpty = true,
                    ) {
                        voiceRecording()
                    }
                } else {
                    voiceRecording()
                }
            }
            // To avoid loosing keyboard focus, the IconButton has to be defined here and has to be always enabled.
            val endButtonContentDescription = stringResource(endButtonParams.endButtonContentDescriptionResId)
            IconButton(
                modifier = Modifier
                    .padding(bottom = 5.dp, top = 5.dp, end = 6.dp, start = 6.dp)
                    .size(48.dp)
                    .clearAndSetSemantics {
                        contentDescription = endButtonContentDescription
                        onClick(null, null)
                    },
                onClick = endButtonParams.endButtonClick,
                content = endButtonParams.endButtonContent,
            )
        }
    }
}

@Composable
private fun NotEncryptedBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = CompoundIcons.LockOff(),
            contentDescription = null,
            tint = ElementTheme.colors.iconInfoPrimary,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(CommonStrings.common_not_encrypted),
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun TextFormattingLayout(
    isRoomEncrypted: Boolean?,
    textInput: @Composable () -> Unit,
    dismissTextFormattingButton: @Composable () -> Unit,
    textFormatting: @Composable () -> Unit,
    endButtonParams: EndButtonParams,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (isRoomEncrypted == false) {
            NotEncryptedBadge()
            Spacer(Modifier.height(8.dp))
        }
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
            // To avoid loosing keyboard focus, the IconButton has to be defined here and has to be always enabled.
            val endButtonContentDescription = stringResource(endButtonParams.endButtonContentDescriptionResId)
            IconButton(
                modifier = Modifier
                    .padding(
                        start = 14.dp,
                        end = 6.dp,
                    )
                    .size(48.dp)
                    .clearAndSetSemantics {
                        contentDescription = endButtonContentDescription
                        onClick(null, null)
                    },
                onClick = endButtonParams.endButtonClick,
                content = endButtonParams.endButtonContent,
            )
        }
    }
}

@Composable
private fun TextInputBox(
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    isTextEmpty: Boolean,
    modifier: Modifier = Modifier,
    textInput: @Composable () -> Unit,
) {
    val bgColor = ElementTheme.colors.bgSubtleSecondary
    val borderColor = ElementTheme.colors.borderDisabled
    val roundedCorners = textInputRoundedCornerShape(composerMode = composerMode)

    Column(
        modifier = Modifier
            .clip(roundedCorners)
            .border(0.5.dp, borderColor, roundedCorners)
            .background(color = bgColor)
            .requiredHeightIn(min = 42.dp)
            .fillMaxSize()
            .then(modifier),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(
                composerMode = composerMode,
                onResetComposerMode = onResetComposerMode,
            )
        } else {
            // Top padding for the message composer box
            Spacer(Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .padding(top = 1.dp, bottom = 4.dp, start = 12.dp, end = 12.dp)
                .then(Modifier.testTag(TestTags.textEditor)),
            contentAlignment = Alignment.CenterStart,
        ) {
            textInput()
            if (isTextEmpty && composerMode.showCaptionCompatibilityWarning()) {
                var showBottomSheet by remember { mutableStateOf(false) }
                Icon(
                    modifier = Modifier
                        .clickable { showBottomSheet = true }
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .align(Alignment.CenterEnd),
                    imageVector = CompoundIcons.InfoSolid(),
                    tint = ElementTheme.colors.iconCriticalPrimary,
                    contentDescription = stringResource(CommonStrings.a11y_info),
                )
                if (showBottomSheet) {
                    CaptionWarningBottomSheet(
                        onDismiss = { showBottomSheet = false },
                    )
                }
            }
        }
    }
}

private fun VoiceMessageState.endButtonKey() = when (this) {
    is VoiceMessageState.Idle -> "Idle"
    is VoiceMessageState.Preview -> "Preview_$isSending"
    is VoiceMessageState.Recording -> "Recording"
}

private fun aTextEditorStateMarkdownList(isRoomEncrypted: Boolean? = null) = persistentListOf(
    aTextEditorStateMarkdown(initialText = "", initialFocus = true, isRoomEncrypted = isRoomEncrypted),
    aTextEditorStateMarkdown(initialText = "A message", initialFocus = true, isRoomEncrypted = isRoomEncrypted),
    aTextEditorStateMarkdown(
        initialText = "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
        initialFocus = true,
        isRoomEncrypted = isRoomEncrypted,
    ),
    aTextEditorStateMarkdown(initialText = "A message without focus", initialFocus = false, isRoomEncrypted = isRoomEncrypted),
)

private fun aTextEditorStateRichList(isRoomEncrypted: Boolean? = null) = persistentListOf(
    aTextEditorStateRich(initialFocus = true, isRoomEncrypted = isRoomEncrypted),
    aTextEditorStateRich(initialText = "A message", initialFocus = true, isRoomEncrypted = isRoomEncrypted),
    aTextEditorStateRich(
        initialText = "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
        initialFocus = true,
        isRoomEncrypted = isRoomEncrypted,
    ),
    aTextEditorStateRich(initialText = "A message without focus", initialFocus = false, isRoomEncrypted = isRoomEncrypted),
)

@PreviewsDayNight
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateMarkdownList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerSimpleNotEncryptedPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateMarkdownList(isRoomEncrypted = false),
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingNotEncryptedPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList(isRoomEncrypted = false)
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEdit(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditNotEncryptedPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList(isRoomEncrypted = false)
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEdit(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditCaptionPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEditCaption(
                // Set an existing caption so that the UI will be in edit caption mode
                content = "An existing caption",
            ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerAddCaptionPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEditCaption(
                // No caption so that the UI will be in add caption mode
                content = "",
            ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MarkdownTextComposerEditPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateMarkdownList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEdit(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerReplyPreview(@PreviewParameter(InReplyToDetailsProvider::class) inReplyToDetails: InReplyToDetails) = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeReply(
                replyToDetails = inReplyToDetails,
            ),
        )
    }
}

@Preview(
    name = DAY_MODE_NAME,
    heightDp = 800,
)
@Preview(
    name = NIGHT_MODE_NAME,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800,
)
@Composable
internal fun TextComposerReplyNotEncryptedPreview(@PreviewParameter(InReplyToDetailsProvider::class) inReplyToDetails: InReplyToDetails) = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList(isRoomEncrypted = false)
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeReply(
                replyToDetails = inReplyToDetails,
            ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerCaptionPreview() = ElementPreview {
    val list = aTextEditorStateMarkdownList()
    PreviewColumn(
        items = list,
    ) { textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Attachment,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerVoicePreview() = ElementPreview {
    PreviewColumn(
        items = persistentListOf(
            VoiceMessageState.Recording(
                duration = 61.seconds,
                levels = WaveFormSamples.realisticWaveForm,
            ),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = false,
                showCursor = false,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 0.seconds,
                playbackProgress = 0.0f,
            ),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = true,
                showCursor = true,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 3.seconds,
                playbackProgress = 0.2f,
            ),
            VoiceMessageState.Preview(
                isSending = true,
                isPlaying = false,
                showCursor = false,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 61.seconds,
                playbackProgress = 0.0f,
            ),
        )
    ) { voiceMessageState ->
        ATextComposer(
            state = aTextEditorStateRich(initialFocus = true),
            voiceMessageState = voiceMessageState,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerVoiceNotEncryptedPreview() = ElementPreview {
    PreviewColumn(
        items = persistentListOf(
            VoiceMessageState.Recording(
                duration = 61.seconds,
                levels = WaveFormSamples.realisticWaveForm,
            ),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = false,
                showCursor = false,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 0.seconds,
                playbackProgress = 0.0f
            ),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = true,
                showCursor = true,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 3.seconds,
                playbackProgress = 0.2f
            ),
            VoiceMessageState.Preview(
                isSending = true,
                isPlaying = false,
                showCursor = false,
                waveform = WaveFormSamples.realisticWaveForm,
                time = 61.seconds,
                playbackProgress = 0.0f
            ),
        )
    ) { voiceMessageState ->
        ATextComposer(
            state = aTextEditorStateRich(initialFocus = true, isRoomEncrypted = false),
            voiceMessageState = voiceMessageState,
            composerMode = MessageComposerMode.Normal,
        )
    }
}

@Preview
@Composable
internal fun TextComposerScaledDensityWithReplyPreview() {
    ElementPreview {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = 3f,
                fontScale = 1.25f,
            ),
        ) {
            val replyToDetails = InReplyToDetails.Ready(
                eventId = EventId("\$1234"),
                senderId = UserId("@alice:example.com"),
                senderProfile = aProfileDetailsReady(),
                eventContent = MessageContent(
                    body = "Message which are being replied, and which was long enough to be displayed on two lines (only!).",
                    inReplyTo = null,
                    isEdited = false,
                    threadInfo = null,
                    type = TextMessageType("Message which are being replied, and which was long enough to be displayed on two lines (only!).", null)
                ),
                textContent = "Message which are being replied, and which was long enough to be displayed on two lines (only!).",
            )
            Box(modifier = Modifier.width(480.dp).height(120.dp)) {
                ATextComposer(
                    state = aTextEditorStateMarkdown(initialText = "", initialFocus = true),
                    voiceMessageState = VoiceMessageState.Idle,
                    composerMode = MessageComposerMode.Reply(replyToDetails, hideImage = false),
                )
            }
        }
    }
}

@Composable
private fun <T> PreviewColumn(
    items: ImmutableList<T>,
    view: @Composable (T) -> Unit,
) {
    Column {
        items.forEach { item ->
            HorizontalDivider()
            Box(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                view(item)
            }
        }
    }
}

@Composable
private fun ATextComposer(
    state: TextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    showTextFormatting: Boolean = false,
) {
    TextComposer(
        state = state,
        showTextFormatting = showTextFormatting,
        voiceMessageState = voiceMessageState,
        composerMode = composerMode,
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
        onTyping = {},
        onReceiveSuggestion = {},
        resolveMentionDisplay = { _, _ -> TextDisplay.Plain },
        resolveAtRoomMentionDisplay = { TextDisplay.Plain },
        onSelectRichContent = null,
    )
}

fun aMessageComposerModeEdit(
    eventOrTransactionId: EventOrTransactionId = EventId("$1234").toEventOrTransactionId(),
    content: String = "Some text",
) = MessageComposerMode.Edit(
    eventOrTransactionId = eventOrTransactionId,
    content = content
)

fun aMessageComposerModeEditCaption(
    eventOrTransactionId: EventOrTransactionId = EventId("$1234").toEventOrTransactionId(),
    content: String,
) = MessageComposerMode.EditCaption(
    eventOrTransactionId = eventOrTransactionId,
    content = content,
)

fun aMessageComposerModeReply(
    replyToDetails: InReplyToDetails,
    hideImage: Boolean = false,
) = MessageComposerMode.Reply(
    replyToDetails = replyToDetails,
    hideImage = hideImage,
)
