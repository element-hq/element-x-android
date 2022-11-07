package io.element.android.x.features.messages.model

import io.element.android.x.designsystem.components.avatar.AvatarData

sealed interface MessagesTimelineItemState {
    data class Virtual(
        val id: String
    ) : MessagesTimelineItemState

    data class MessageEvent(
        val id: String = "",
        val sender: String = "",
        val senderAvatar: AvatarData? = null,
        val content: String? = null,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val groupPosition: MessagesItemGroupPosition = MessagesItemGroupPosition.None
    ) : MessagesTimelineItemState

}



