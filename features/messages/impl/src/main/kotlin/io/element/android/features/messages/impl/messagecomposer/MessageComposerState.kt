/*
 * Copyright (c) 2022 New Vector Ltd
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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
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
    val memberSuggestions: ImmutableList<ResolvedMentionSuggestion>,
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
