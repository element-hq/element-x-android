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

package io.element.android.libraries.textcomposer.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

@Immutable
sealed interface VoiceMessageState {
    data object Idle : VoiceMessageState

    data class Preview(
        val isSending: Boolean,
        val isPlaying: Boolean,
        val showCursor: Boolean,
        val playbackProgress: Float,
        val time: Duration,
        val waveform: ImmutableList<Float>,
    ) : VoiceMessageState

    data class Recording(
        val duration: Duration,
        val levels: ImmutableList<Float>,
    ) : VoiceMessageState
}
