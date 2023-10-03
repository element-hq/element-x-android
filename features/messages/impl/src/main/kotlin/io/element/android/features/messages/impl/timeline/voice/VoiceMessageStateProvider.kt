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

package io.element.android.features.messages.impl.timeline.voice

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class VoiceMessageStateProvider: PreviewParameterProvider<VoiceMessageState> {
    override val values: Sequence<VoiceMessageState>
        get() = sequenceOf(
            VoiceMessageState(
                isLoading = true,
                isError = false,
                isPlaying = false,
                progress = 0f,
                elapsed = "00:00",
                eventSink = {},
            ),
            VoiceMessageState(
                isLoading = false,
                isError = true,
                isPlaying = false,
                progress = 0.5f,
                elapsed = "00:00",
                eventSink = {}
            ),
            VoiceMessageState(
                isLoading = false,
                isError = false,
                isPlaying = true,
                progress = 1f,
                elapsed = "00:00",
                eventSink = {}
            ),
            VoiceMessageState(
                isLoading = false,
                isError = false,
                isPlaying = false,
                progress = 0.2f,
                elapsed = "00:00",
                eventSink = {}
            ),
        )
}
