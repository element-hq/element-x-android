package io.element.android.x.features.messages.model

import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.content.MessagesTimelineItemContent

sealed interface MessagesTimelineItemState {
    data class Virtual(
        val id: String
    ) : MessagesTimelineItemState

    data class MessageEvent(
        val id: String,
        val senderId: String,
        val senderDisplayName: String?,
        val senderAvatar: AvatarData,
        val content: MessagesTimelineItemContent,
        val sentTime: String = "",
        val isMine: Boolean = false,
        val groupPosition: MessagesItemGroupPosition = MessagesItemGroupPosition.None,
        val reactionsState: MessagesItemReactionState
    ) : MessagesTimelineItemState {

        val showSenderInformation = groupPosition.isNew() && !isMine

        val safeSenderName: String = senderDisplayName ?: senderId
    }
}



