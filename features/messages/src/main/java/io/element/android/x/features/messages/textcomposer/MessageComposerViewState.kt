package io.element.android.x.features.messages.textcomposer

import com.airbnb.mvrx.MavericksState

data class MessageComposerViewState(
    // val roomId: String,
    // val canSendMessage: CanSendStatus = CanSendStatus.Allowed,
    val isSendButtonVisible: Boolean = false,
    val rootThreadEventId: String? = null,
    val startsThread: Boolean = false,
    // val sendMode: SendMode = SendMode.Regular("", false),
    // val voiceRecordingUiState: VoiceMessageRecorderView.RecordingUiState = VoiceMessageRecorderView.RecordingUiState.Idle,
    // val voiceBroadcastState: VoiceBroadcastState? = null,
    val text: CharSequence? = null,
    val isFullScreen: Boolean = false,
) : MavericksState