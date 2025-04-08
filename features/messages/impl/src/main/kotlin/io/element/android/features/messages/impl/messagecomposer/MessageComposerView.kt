/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerEvents
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerStateProvider
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.TextComposer
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import kotlinx.coroutines.launch

@Composable
internal fun MessageComposerView(
    state: MessageComposerState,
    voiceMessageState: VoiceMessageComposerState,
    subcomposing: Boolean,
    enableVoiceMessages: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    fun sendMessage() {
        state.eventSink(MessageComposerEvents.SendMessage)
    }

    fun sendUri(uri: Uri) {
        state.eventSink(MessageComposerEvents.SendUri(uri))
    }

    fun onAddAttachment() {
        state.eventSink(MessageComposerEvents.AddAttachment)
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvents.CloseSpecialMode)
    }

    fun onDismissTextFormatting() {
        view.clearFocus()
        state.eventSink(MessageComposerEvents.ToggleTextFormatting(enabled = false))
    }

    fun onSuggestionReceived(suggestion: Suggestion?) {
        state.eventSink(MessageComposerEvents.SuggestionReceived(suggestion))
    }

    fun onError(error: Throwable) {
        state.eventSink(MessageComposerEvents.Error(error))
    }

    fun onTyping(typing: Boolean) {
        state.eventSink(MessageComposerEvents.TypingNotice(typing))
    }

    val coroutineScope = rememberCoroutineScope()
    fun onRequestFocus() {
        coroutineScope.launch {
            state.textEditorState.requestFocus()
        }
    }

    val onVoiceRecorderEvent = { press: VoiceMessageRecorderEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvents.RecorderEvent(press))
    }

    val onSendVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
    }

    val onDeleteVoiceMessage = {
        voiceMessageState.eventSink(VoiceMessageComposerEvents.DeleteVoiceMessage)
    }

    val onVoicePlayerEvent = { event: VoiceMessagePlayerEvent ->
        voiceMessageState.eventSink(VoiceMessageComposerEvents.PlayerEvent(event))
    }

    TextComposer(
        modifier = modifier,
        state = state.textEditorState,
        voiceMessageState = voiceMessageState.voiceMessageState,
        subcomposing = subcomposing,
        onRequestFocus = ::onRequestFocus,
        onSendMessage = ::sendMessage,
        composerMode = state.mode,
        showTextFormatting = state.showTextFormatting,
        onResetComposerMode = ::onCloseSpecialMode,
        onAddAttachment = ::onAddAttachment,
        onDismissTextFormatting = ::onDismissTextFormatting,
        enableVoiceMessages = enableVoiceMessages,
        onVoiceRecorderEvent = onVoiceRecorderEvent,
        onVoicePlayerEvent = onVoicePlayerEvent,
        onSendVoiceMessage = onSendVoiceMessage,
        onDeleteVoiceMessage = onDeleteVoiceMessage,
        onReceiveSuggestion = ::onSuggestionReceived,
        resolveMentionDisplay = state.resolveMentionDisplay,
        resolveAtRoomMentionDisplay = state.resolveAtRoomMentionDisplay,
        onError = ::onError,
        onTyping = ::onTyping,
        onSelectRichContent = ::sendUri,
    )
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewPreview(
    @PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
            enableVoiceMessages = true,
            subcomposing = false,
        )
        MessageComposerView(
            modifier = Modifier.height(200.dp),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
            enableVoiceMessages = true,
            subcomposing = false,
        )
        DisabledComposerView()
    }
}

@PreviewsDayNight
@Composable
internal fun MessageComposerViewVoicePreview(
    @PreviewParameter(VoiceMessageComposerStateProvider::class) state: VoiceMessageComposerState,
) = ElementPreview {
    Column {
        MessageComposerView(
            modifier = Modifier.height(IntrinsicSize.Min),
            state = aMessageComposerState(),
            voiceMessageState = state,
            enableVoiceMessages = true,
            subcomposing = false,
        )
    }
}
