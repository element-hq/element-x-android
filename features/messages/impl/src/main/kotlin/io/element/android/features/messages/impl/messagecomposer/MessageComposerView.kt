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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.textcomposer.TextComposer

@Composable
fun MessageComposerView(
    state: MessageComposerState,
    onSendLocationClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onFullscreenToggle() {
        state.eventSink(MessageComposerEvents.ToggleFullScreenState)
    }

    fun sendMessage(message: String) {
        state.eventSink(MessageComposerEvents.SendMessage(message))
    }

    fun onCloseSpecialMode() {
        state.eventSink(MessageComposerEvents.CloseSpecialMode)
    }

    fun onComposerTextChange(text: CharSequence) {
        state.eventSink(MessageComposerEvents.UpdateText(text))
    }

    Box {
        AttachmentsBottomSheet(
            state = state,
            onSendLocationClicked = onSendLocationClicked,
        )

        TextComposer(
            onSendMessage = ::sendMessage,
            composerMode = state.mode,
            onResetComposerMode = ::onCloseSpecialMode,
            onComposerTextChange = ::onComposerTextChange,
            onAddAttachment = {
                state.eventSink(MessageComposerEvents.AddAttachment)
            },
            composerCanSendMessage = state.isSendButtonVisible,
            composerText = state.text?.charSequence?.toString(),
            modifier = modifier
        )
    }
}

@Preview
@Composable
internal fun MessageComposerViewLightPreview(@PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun MessageComposerViewDarkPreview(@PreviewParameter(MessageComposerStateProvider::class) state: MessageComposerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: MessageComposerState) {
    MessageComposerView(
        state = state,
        onSendLocationClicked = {}
    )
}
