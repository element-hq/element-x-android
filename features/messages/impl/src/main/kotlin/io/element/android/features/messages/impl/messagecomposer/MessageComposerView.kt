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
import io.element.android.libraries.textcomposer.model.Message
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import kotlinx.coroutines.launch

@Composable
internal fun MessageComposerView(
    state: MessageComposerState,
    voiceMessageState: VoiceMessageComposerState,
    subcomposing: Boolean,
    enableTextFormatting: Boolean,
    enableVoiceMessages: Boolean,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    fun sendMessage(message: Message) {
        state.eventSink(MessageComposerEvents.SendMessage(message))
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

    val coroutineScope = rememberCoroutineScope()
    fun onRequestFocus() {
        coroutineScope.launch {
            state.richTextEditorState.requestFocus()
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
        state = state.richTextEditorState,
        voiceMessageState = voiceMessageState.voiceMessageState,
        subcomposing = subcomposing,
        onRequestFocus = ::onRequestFocus,
        onSendMessage = ::sendMessage,
        composerMode = state.mode,
        showTextFormatting = state.showTextFormatting,
        onResetComposerMode = ::onCloseSpecialMode,
        onAddAttachment = ::onAddAttachment,
        onDismissTextFormatting = ::onDismissTextFormatting,
        enableTextFormatting = enableTextFormatting,
        enableVoiceMessages = enableVoiceMessages,
        onVoiceRecorderEvent = onVoiceRecorderEvent,
        onVoicePlayerEvent = onVoicePlayerEvent,
        onSendVoiceMessage = onSendVoiceMessage,
        onDeleteVoiceMessage = onDeleteVoiceMessage,
        onSuggestionReceived = ::onSuggestionReceived,
        onError = ::onError,
        currentUserId = state.currentUserId,
        onRichContentSelected = ::sendUri,
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
            enableTextFormatting = true,
            enableVoiceMessages = true,
            subcomposing = false,
        )
        MessageComposerView(
            modifier = Modifier.height(200.dp),
            state = state,
            voiceMessageState = aVoiceMessageComposerState(),
            enableTextFormatting = true,
            enableVoiceMessages = true,
            subcomposing = false,
        )
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
            enableTextFormatting = true,
            enableVoiceMessages = true,
            subcomposing = false,
        )
    }
}
