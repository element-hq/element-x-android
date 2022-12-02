package io.element.android.x.features.messages.model

import androidx.compose.runtime.Stable
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.textcomposer.MessageComposerMode

@Stable
data class MessagesViewState(
    val roomId: String,
    val roomName: String? = null,
    val roomAvatar: AvatarData? = null,
    val timelineItems: Async<List<MessagesTimelineItemState>> = Uninitialized,
    val hasMoreToLoad: Boolean = true,
    val itemActionsSheetState: Async<MessagesItemActionsSheetState> = Uninitialized,
    val snackbarContent: String? = null,
    val highlightedEventId: String? = null,
    val composerMode: MessageComposerMode = MessageComposerMode.Normal(""),
) : MavericksState {

    @Suppress("unused")
    constructor(roomId: String) : this(
        roomId = roomId,
        roomName = null,
        roomAvatar = null
    )
}
