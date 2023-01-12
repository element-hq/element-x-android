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

package io.element.android.x.features.messages.textcomposer

import androidx.compose.runtime.Immutable
import io.element.android.x.core.data.StableCharSequence
import io.element.android.x.textcomposer.MessageComposerMode

@Immutable
data class MessageComposerState(
    // val roomId: String,
    // val canSendMessage: CanSendStatus = CanSendStatus.Allowed,
    val isSendButtonVisible: Boolean = false,
    val rootThreadEventId: String? = null,
    val startsThread: Boolean = false,
    // val sendMode: SendMode = SendMode.Regular("", false),
    // val voiceRecordingUiState: VoiceMessageRecorderView.RecordingUiState = VoiceMessageRecorderView.RecordingUiState.Idle,
    // val voiceBroadcastState: VoiceBroadcastState? = null,
    val text: StableCharSequence? = null,
    val isFullScreen: Boolean = false,
    val mode: MessageComposerMode = MessageComposerMode.Normal(""),
    val eventSink: (MessageComposerEvents) -> Unit = {}
)
