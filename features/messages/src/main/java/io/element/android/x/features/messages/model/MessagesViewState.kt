package io.element.android.x.features.messages.model

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.designsystem.components.avatar.AvatarData

data class MessagesViewState(
    val roomId: String,
    val roomName: String? = null,
    val roomAvatar: AvatarData? = null,
    val timelineItems: Async<List<MessagesTimelineItemState>> = Uninitialized,
    val hasMoreToLoad: Boolean = true,
    val composerFullScreen: Boolean = false,
) : MavericksState {

    @Suppress("unused")
    constructor(roomId: String) : this(
        roomId = roomId,
        roomName = null,
        roomAvatar = null
    )
}
