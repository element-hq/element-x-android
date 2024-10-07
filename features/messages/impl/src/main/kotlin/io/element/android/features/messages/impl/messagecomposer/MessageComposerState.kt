/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList

@Stable
data class MessageComposerState(
    val textEditorState: TextEditorState,
    val isFullScreen: Boolean,
    val mode: MessageComposerMode,
    val showAttachmentSourcePicker: Boolean,
    val showTextFormatting: Boolean,
    val canShareLocation: Boolean,
    val canCreatePoll: Boolean,
    val attachmentsState: AttachmentsState,
    val suggestions: ImmutableList<ResolvedSuggestion>,
    val resolveMentionDisplay: (String, String) -> TextDisplay,
    val eventSink: (MessageComposerEvents) -> Unit,
)

@Immutable
sealed interface AttachmentsState {
    data object None : AttachmentsState
    data class Previewing(val attachments: ImmutableList<Attachment>) : AttachmentsState
    sealed interface Sending : AttachmentsState {
        data class Processing(val attachments: ImmutableList<Attachment>) : Sending
        data class Uploading(val progress: Float) : Sending
    }
}
