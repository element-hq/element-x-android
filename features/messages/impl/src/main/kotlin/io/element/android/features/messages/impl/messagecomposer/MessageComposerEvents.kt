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

import androidx.compose.runtime.Immutable
import io.element.android.libraries.textcomposer.MessageComposerMode

@Immutable
sealed interface MessageComposerEvents {
    object ToggleFullScreenState : MessageComposerEvents
    data class SendMessage(val message: String) : MessageComposerEvents
    object CloseSpecialMode : MessageComposerEvents
    data class SetMode(val composerMode: MessageComposerMode) : MessageComposerEvents
    data class UpdateText(val text: CharSequence) : MessageComposerEvents
    object AddAttachment : MessageComposerEvents
    object DismissAttachmentMenu : MessageComposerEvents
    sealed interface PickAttachmentSource : MessageComposerEvents {
        object FromGallery : PickAttachmentSource
        object FromFiles : PickAttachmentSource
        object PhotoFromCamera : PickAttachmentSource
        object VideoFromCamera : PickAttachmentSource
        object Location : PickAttachmentSource
    }
}
