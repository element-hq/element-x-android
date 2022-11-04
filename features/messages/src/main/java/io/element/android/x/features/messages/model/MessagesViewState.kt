package io.element.android.x.features.messages.model

import com.airbnb.mvrx.MavericksState
import io.element.android.x.designsystem.components.avatar.AvatarData

data class MessagesViewState(
    val roomId: String,
    val roomName: String? = null,
    val roomAvatar: AvatarData? = null
) : MavericksState {

    @Suppress("unused")
    constructor(roomId: String) : this(roomId = roomId, roomName = null, roomAvatar = null)

}
