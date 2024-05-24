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
import androidx.compose.runtime.Immutable
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion

@Immutable
sealed interface MessageComposerEvents {
    data object ToggleFullScreenState : MessageComposerEvents
    data object SendMessage : MessageComposerEvents
    data class SendUri(val uri: Uri) : MessageComposerEvents
    data object CloseSpecialMode : MessageComposerEvents
    data class SetMode(val composerMode: MessageComposerMode) : MessageComposerEvents
    data object AddAttachment : MessageComposerEvents
    data object DismissAttachmentMenu : MessageComposerEvents
    sealed interface PickAttachmentSource : MessageComposerEvents {
        data object FromGallery : PickAttachmentSource
        data object FromFiles : PickAttachmentSource
        data object PhotoFromCamera : PickAttachmentSource
        data object VideoFromCamera : PickAttachmentSource
        data object Location : PickAttachmentSource
        data object Poll : PickAttachmentSource
    }
    data class ToggleTextFormatting(val enabled: Boolean) : MessageComposerEvents
    data object CancelSendAttachment : MessageComposerEvents
    data class Error(val error: Throwable) : MessageComposerEvents
    data class TypingNotice(val isTyping: Boolean) : MessageComposerEvents
    data class SuggestionReceived(val suggestion: Suggestion?) : MessageComposerEvents
    data class InsertMention(val mention: ResolvedMentionSuggestion) : MessageComposerEvents
}
