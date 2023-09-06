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
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.textcomposer.MessageComposerMode
import io.element.android.libraries.textcomposer.TextComposerState
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class MessageComposerState(
    val composerState: TextComposerState,
    val isFullScreen: Boolean,
    val hasFocus: Boolean,
    val mode: MessageComposerMode,
    val showAttachmentSourcePicker: Boolean,
    val canShareLocation: Boolean,
    val canCreatePoll: Boolean,
    val attachmentsState: AttachmentsState,
    val eventSink: (MessageComposerEvents) -> Unit
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
