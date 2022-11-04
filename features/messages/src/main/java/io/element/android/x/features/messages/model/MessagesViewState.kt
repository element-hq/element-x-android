package io.element.android.x.features.messages.model

import com.airbnb.mvrx.MavericksState
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.matrix.core.RoomId

data class MessagesViewState(
    val roomId: String,
    val roomTitle: String = "",
    val roomAvatar: AvatarData? = null
) : MavericksState {

    @Suppress("unused")
    constructor(roomId: String) : this(roomId = roomId, roomTitle = "", roomAvatar = null)

}
