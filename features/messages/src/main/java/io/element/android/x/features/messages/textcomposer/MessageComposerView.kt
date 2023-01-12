package io.element.android.x.features.messages.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.textcomposer.TextComposer

@Composable
fun MessageComposerView(
    state: MessageComposerState,
    modifier: Modifier
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
        modifier = modifier
    )
}
