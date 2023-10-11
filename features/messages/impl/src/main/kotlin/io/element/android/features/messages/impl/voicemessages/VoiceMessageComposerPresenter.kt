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

package io.element.android.features.messages.impl.voicemessages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import javax.inject.Inject

@SingleIn(RoomScope::class)
class VoiceMessageComposerPresenter @Inject constructor() : Presenter<VoiceMessageComposerState> {
    @Composable
    override fun present(): VoiceMessageComposerState {
        var voiceMessageState by remember { mutableStateOf<VoiceMessageState>(VoiceMessageState.Idle) }

        fun onRecordButtonPress(event: VoiceMessageComposerEvents.RecordButtonEvent) = when(event.pressEvent) {
            PressEvent.PressStart ->  {
                // TODO start the recording
                voiceMessageState = VoiceMessageState.Recording
            }
            PressEvent.LongPressEnd -> {
                // TODO finish the recording
                voiceMessageState = VoiceMessageState.Idle
            }
            PressEvent.Tapped -> {
                // TODO discard the recording and show the 'hold to record' tooltip
                voiceMessageState = VoiceMessageState.Idle
            }
        }


        fun handleEvents(event: VoiceMessageComposerEvents) {
            when (event) {
                is VoiceMessageComposerEvents.RecordButtonEvent -> onRecordButtonPress(event)
            }
        }

        return VoiceMessageComposerState(
            voiceMessageState = voiceMessageState,
            eventSink = { handleEvents(it) }
        )
    }
}
