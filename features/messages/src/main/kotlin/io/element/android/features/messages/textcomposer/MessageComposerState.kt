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

package io.element.android.features.messages.textcomposer

import androidx.compose.runtime.Immutable
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.textcomposer.MessageComposerMode

@Immutable
data class MessageComposerState(
    val text: StableCharSequence?,
    val isFullScreen: Boolean,
    val mode: MessageComposerMode,
    val eventSink: (MessageComposerEvents) -> Unit
) {
    val isSendButtonVisible: Boolean = text?.charSequence.isNullOrEmpty().not()
}

fun aMessageComposerState() = MessageComposerState(
    text = StableCharSequence(""),
    isFullScreen = false,
    mode = MessageComposerMode.Normal(content = ""),
    eventSink = {}
)
