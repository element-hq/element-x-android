/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
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
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetailsProvider
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
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import uniffi.wysiwyg_composer.MenuAction
import kotlin.time.Duration.Companion.seconds

@Composable
fun TextComposer(
    state: TextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    enableVoiceMessages: Boolean,
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
    modifier: Modifier = Modifier,
    showTextFormatting: Boolean = false,
    subcomposing: Boolean = false,
) {
    val markdown = when (state) {
        is TextEditorState.Markdown -> state.state.text.value()
        is TextEditorState.Rich -> state.richTextEditorState.messageMarkdown
    }
    val onSendClick = {
        onSendMessage()
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

    val composerOptionsButton: @Composable () -> Unit = remember(composerMode) {
        @Composable {
            when (composerMode) {
                is MessageComposerMode.Attachment -> {
                    Spacer(modifier = Modifier.width(9.dp))
                }
                is MessageComposerMode.EditCaption -> {
                    Spacer(modifier = Modifier.width(16.dp))
                }
                else -> {
                    ComposerOptionsButton(
                        modifier = Modifier
                            .size(48.dp),
                        onClick = onAddAttachment
                    )
                }
            }
        }
    }

    val placeholder = if (composerMode.inThread) {
        stringResource(id = CommonStrings.action_reply_in_thread)
    } else if (composerMode is MessageComposerMode.Attachment || composerMode is MessageComposerMode.EditCaption) {
        stringResource(id = R.string.rich_text_editor_composer_caption_placeholder)
    } else {
        stringResource(id = R.string.rich_text_editor_composer_placeholder)
    }
    val textInput: @Composable () -> Unit = if ((composerMode as? MessageComposerMode.Attachment)?.allowCaption == false) {
        {
            // No text input when in attachment mode and caption not allowed.
        }
    } else {
        when (state) {
            is TextEditorState.Rich -> {
                remember(state.richTextEditorState, subcomposing, composerMode, onResetComposerMode, onError) {
                    @Composable {
                        TextInput(
                            state = state.richTextEditorState,
                            subcomposing = subcomposing,
                            placeholder = placeholder,
                            composerMode = composerMode,
                            onResetComposerMode = onResetComposerMode,
                            resolveMentionDisplay = resolveMentionDisplay,
                            resolveRoomMentionDisplay = { resolveMentionDisplay("@room", "#") },
                            onError = onError,
                            onTyping = onTyping,
                            onSelectRichContent = onSelectRichContent,
                        )
                    }
                }
            }
            is TextEditorState.Markdown -> {
                @Composable {
                    val style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.hasFocus())
                    TextInputBox(
                        composerMode = composerMode,
                        onResetComposerMode = onResetComposerMode,
                        placeholder = placeholder,
                        showPlaceholder = state.state.text.value().isEmpty(),
                        subcomposing = subcomposing,
                    ) {
                        MarkdownTextInput(
                            state = state.state,
                            subcomposing = subcomposing,
                            onTyping = onTyping,
                            onReceiveSuggestion = onReceiveSuggestion,
                            richTextEditorStyle = style,
                            onSelectRichContent = onSelectRichContent,
                        )
                    }
                }
            }
        }
    }

    val canSendMessage = markdown.isNotBlank() || composerMode is MessageComposerMode.Attachment
    val sendButton = @Composable {
        SendButton(
            canSendMessage = canSendMessage,
            onClick = onSendClick,
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

    val textFormattingOptions: @Composable (() -> Unit)? = (state as? TextEditorState.Rich)?.let {
        @Composable { TextFormatting(state = it.richTextEditorState) }
    }

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
        else -> sendButton
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

    if (showTextFormatting && textFormattingOptions != null) {
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
private fun TextInputBox(
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    placeholder: String,
    showPlaceholder: Boolean,
    subcomposing: Boolean,
    textInput: @Composable BoxScope.() -> Unit,
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
            .fillMaxSize(),
    ) {
        if (composerMode is MessageComposerMode.Special) {
            ComposerModeView(
                composerMode = composerMode,
                onResetComposerMode = onResetComposerMode,
            )
        }
        val defaultTypography = ElementTheme.typography.fontBodyLgRegular
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 12.dp, end = 12.dp)
                // Apply test tag only once, otherwise 2 nodes will have it (both the normal and subcomposing one) and tests will fail
                .then(if (!subcomposing) Modifier.testTag(TestTags.textEditor) else Modifier),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Placeholder
            if (showPlaceholder) {
                Text(
                    text = placeholder,
                    style = defaultTypography.copy(
                        color = ElementTheme.colors.textSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            textInput()

            if (showPlaceholder && composerMode.showCaptionCompatibilityWarning()) {
                var showBottomSheet by remember { mutableStateOf(false) }
                Icon(
                    modifier = Modifier
                        .clickable { showBottomSheet = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.CenterEnd),
                    imageVector = CompoundIcons.InfoSolid(),
                    tint = ElementTheme.colors.iconCriticalPrimary,
                    contentDescription = null,
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

@Composable
private fun TextInput(
    state: RichTextEditorState,
    subcomposing: Boolean,
    placeholder: String,
    composerMode: MessageComposerMode,
    onResetComposerMode: () -> Unit,
    resolveRoomMentionDisplay: () -> TextDisplay,
    resolveMentionDisplay: (text: String, url: String) -> TextDisplay,
    onError: (Throwable) -> Unit,
    onTyping: (Boolean) -> Unit,
    onSelectRichContent: ((Uri) -> Unit)?,
) {
    TextInputBox(
        composerMode = composerMode,
        onResetComposerMode = onResetComposerMode,
        placeholder = placeholder,
        showPlaceholder = state.messageHtml.isEmpty(),
        subcomposing = subcomposing,
    ) {
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
            onRichContentSelected = onSelectRichContent,
            onTyping = onTyping,
        )
    }
}

private fun aTextEditorStateMarkdownList() = persistentListOf(
    aTextEditorStateMarkdown(initialText = "", initialFocus = true),
    aTextEditorStateMarkdown(initialText = "A message", initialFocus = true),
    aTextEditorStateMarkdown(
        initialText = "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
        initialFocus = true,
    ),
    aTextEditorStateMarkdown(initialText = "A message without focus", initialFocus = false),
)

private fun aTextEditorStateRichList() = persistentListOf(
    aTextEditorStateRich(initialFocus = true),
    aTextEditorStateRich(initialText = "A message", initialFocus = true),
    aTextEditorStateRich(
        initialText = "A message\nWith several lines\nTo preview larger textfields and long lines with overflow",
        initialFocus = true
    ),
    aTextEditorStateRich(initialText = "A message without focus", initialFocus = false),
)

@PreviewsDayNight
@Composable
internal fun TextComposerSimplePreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateMarkdownList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Normal,
            enableVoiceMessages = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerFormattingPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            showTextFormatting = true,
            composerMode = MessageComposerMode.Normal,
            enableVoiceMessages = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEdit(),
            enableVoiceMessages = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerEditCaptionPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEditCaption(
                // Set an existing caption so that the UI will be in edit caption mode
                content = "An existing caption",
            ),
            enableVoiceMessages = false,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerAddCaptionPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { index, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEditCaption(
                // No caption so that the UI will be in add caption mode
                content = "",
                showCompatibilityWarning = index == 0,
            ),
            enableVoiceMessages = false,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MarkdownTextComposerEditPreview() = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateMarkdownList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeEdit(),
            enableVoiceMessages = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerReplyPreview(@PreviewParameter(InReplyToDetailsProvider::class) inReplyToDetails: InReplyToDetails) = ElementPreview {
    PreviewColumn(
        items = aTextEditorStateRichList()
    ) { _, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = aMessageComposerModeReply(
                replyToDetails = inReplyToDetails,
            ),
            enableVoiceMessages = true,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerCaptionPreview() = ElementPreview {
    val list = aTextEditorStateMarkdownList()
    PreviewColumn(
        items = (list + aTextEditorStateMarkdown(initialText = "NO_CAPTION", initialFocus = true)).toPersistentList()
    ) { index, textEditorState ->
        ATextComposer(
            state = textEditorState,
            voiceMessageState = VoiceMessageState.Idle,
            composerMode = MessageComposerMode.Attachment(
                allowCaption = index < list.size,
                showCaptionCompatibilityWarning = index == 0,
            ),
            enableVoiceMessages = false,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TextComposerVoicePreview() = ElementPreview {
    PreviewColumn(
        items = persistentListOf(
            VoiceMessageState.Recording(61.seconds, createFakeWaveform()),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = false,
                showCursor = false,
                waveform = createFakeWaveform(),
                time = 0.seconds,
                playbackProgress = 0.0f
            ),
            VoiceMessageState.Preview(
                isSending = false,
                isPlaying = true,
                showCursor = true,
                waveform = createFakeWaveform(),
                time = 3.seconds,
                playbackProgress = 0.2f
            ),
            VoiceMessageState.Preview(
                isSending = true,
                isPlaying = false,
                showCursor = false,
                waveform = createFakeWaveform(),
                time = 61.seconds,
                playbackProgress = 0.0f
            ),
        )
    ) { _, voiceMessageState ->
        ATextComposer(
            state = aTextEditorStateRich(initialFocus = true),
            voiceMessageState = voiceMessageState,
            composerMode = MessageComposerMode.Normal,
            enableVoiceMessages = true,
        )
    }
}

@Composable
private fun <T> PreviewColumn(
    items: ImmutableList<T>,
    view: @Composable (Int, T) -> Unit,
) {
    Column {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                view(index, item)
            }
        }
    }
}

@Composable
private fun ATextComposer(
    state: TextEditorState,
    voiceMessageState: VoiceMessageState,
    composerMode: MessageComposerMode,
    enableVoiceMessages: Boolean,
    showTextFormatting: Boolean = false,
) {
    TextComposer(
        state = state,
        showTextFormatting = showTextFormatting,
        voiceMessageState = voiceMessageState,
        composerMode = composerMode,
        enableVoiceMessages = enableVoiceMessages,
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
    showCompatibilityWarning: Boolean = false,
) = MessageComposerMode.EditCaption(
    eventOrTransactionId = eventOrTransactionId,
    content = content,
    showCaptionCompatibilityWarning = showCompatibilityWarning,
)

fun aMessageComposerModeReply(
    replyToDetails: InReplyToDetails,
    hideImage: Boolean = false,
) = MessageComposerMode.Reply(
    replyToDetails = replyToDetails,
    hideImage = hideImage,
)
