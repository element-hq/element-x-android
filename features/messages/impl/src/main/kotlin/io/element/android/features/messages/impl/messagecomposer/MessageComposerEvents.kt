/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.net.Uri
import androidx.compose.runtime.Immutable
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
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
    data class Error(val error: Throwable) : MessageComposerEvents
    data class TypingNotice(val isTyping: Boolean) : MessageComposerEvents
    data class SuggestionReceived(val suggestion: Suggestion?) : MessageComposerEvents
    data class InsertSuggestion(val resolvedSuggestion: ResolvedSuggestion) : MessageComposerEvents
    data object SaveDraft : MessageComposerEvents
}
