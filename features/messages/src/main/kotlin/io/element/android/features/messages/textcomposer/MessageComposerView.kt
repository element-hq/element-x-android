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

package io.element.android.features.messages.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.textcomposer.TextComposer

@Composable
fun MessageComposerView(
    state: MessageComposerState,
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

    TextComposer(
        onSendMessage = ::sendMessage,
        fullscreen = state.isFullScreen,
        onFullscreenToggle = ::onFullscreenToggle,
        composerMode = state.mode,
        onCloseSpecialMode = ::onCloseSpecialMode,
        onComposerTextChange = ::onComposerTextChange,
        composerCanSendMessage = state.isSendButtonVisible,
        composerText = state.text?.charSequence?.toString(),
        isInDarkMode = !ElementTheme.colors.isLight,
        modifier = modifier
    )
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
    MessageComposerView(state)
}
