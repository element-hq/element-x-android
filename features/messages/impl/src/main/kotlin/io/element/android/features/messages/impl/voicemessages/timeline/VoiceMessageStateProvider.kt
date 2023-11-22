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

package io.element.android.features.messages.impl.voicemessages.timeline

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class VoiceMessageStateProvider : PreviewParameterProvider<VoiceMessageState> {
    override val values: Sequence<VoiceMessageState>
        get() = sequenceOf(
            aVoiceMessageState(
                VoiceMessageState.Button.Downloading,
                progress = 0f,
                time = "0:00",
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Retry,
                progress = 0.5f,
                time = "0:01",
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Play,
                progress = 1f,
                time = "1:00",
                showCursor = true,
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Pause,
                progress = 0.2f,
                time = "10:00",
                showCursor = true,
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Disabled,
                progress = 0.2f,
                time = "30:00",
            ),
        )
}

fun aVoiceMessageState(
    button: VoiceMessageState.Button = VoiceMessageState.Button.Play,
    progress: Float = 0f,
    time: String = "1:00",
    showCursor: Boolean = false,
) = VoiceMessageState(
    button = button,
    progress = progress,
    time = time,
    showCursor = showCursor,
    eventSink = {},
)
