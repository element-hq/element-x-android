package io.element.android.x.features.messages

import androidx.compose.runtime.Immutable
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.actionlist.ActionListState
import io.element.android.x.features.messages.textcomposer.MessageComposerState
import io.element.android.x.features.messages.timeline.TimelineState
import io.element.android.x.matrix.core.RoomId

@Immutable
data class MessagesState(
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatar: AvatarData?,
    val composerState: MessageComposerState,
    val timelineState: TimelineState,
    val actionListState: ActionListState,
    val eventSink: (MessagesEvents) -> Unit
)
