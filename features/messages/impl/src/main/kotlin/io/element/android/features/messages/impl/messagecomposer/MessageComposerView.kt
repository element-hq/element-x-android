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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.textcomposer.Message
import io.element.android.libraries.textcomposer.TextComposer

@Composable
fun MessageComposerView(
    state: MessageComposerState,
    onSendLocationClicked: () -> Unit,
    onCreatePollClicked: () -> Unit,
    enableTextFormatting: Boolean,
    modifier: Modifier = Modifier,
) {
    fun onFullscreenToggle() {
        state.eventSink(MessageComposerEvents.ToggleFullScreenState)
    }

    fun sendMessage(message: Message) {
        state.eventSink(MessageComposerEvents.SendMessage(message))
    }

    fun onAddAttachment() {
        state.eventSink(MessageComposerEvents.AddAttachment)
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvents.CloseSpecialMode)
    }

    fun onDismissTextFormatting() {
        state.eventSink(MessageComposerEvents.ToggleTextFormatting(enabled = false))
    }

    fun onError(error: Throwable) {
        state.eventSink(MessageComposerEvents.Error(error))
    }

    Box(modifier = modifier) {
        AttachmentsBottomSheet(
            state = state,
            onSendLocationClicked = onSendLocationClicked,
            onCreatePollClicked = onCreatePollClicked,
            enableTextFormatting = enableTextFormatting,
        )

        TextComposer(
            state = state.richTextEditorState,
            canSendMessage = state.canSendMessage,
            onRequestFocus = { state.richTextEditorState.requestFocus() },
            onSendMessage = ::sendMessage,
            composerMode = state.mode,
            showTextFormatting = state.showTextFormatting,
            onResetComposerMode = ::onCloseSpecialMode,
            onAddAttachment = ::onAddAttachment,
            onDismissTextFormatting = ::onDismissTextFormatting,
            enableTextFormatting = enableTextFormatting,
            onError = ::onError,
        )
    }
}

@DayNightPreviews
@Composable
internal fun MessageComposerViewPreview(@PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState) = ElementPreview {
    MessageComposerView(
        state = state,
        onSendLocationClicked = {},
        onCreatePollClicked = {},
        enableTextFormatting = true,
    )
}
