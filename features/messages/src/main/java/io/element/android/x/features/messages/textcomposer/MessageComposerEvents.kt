package io.element.android.x.features.messages.textcomposer

import io.element.android.x.textcomposer.MessageComposerMode

sealed interface MessageComposerEvents {
    object ToggleFullScreenState : MessageComposerEvents
    data class SendMessage(val message: String) : MessageComposerEvents
    object CloseSpecialMode : MessageComposerEvents
    data class SetMode(val composerMode: MessageComposerMode) : MessageComposerEvents
    data class UpdateText(val text: CharSequence) : MessageComposerEvents
}
