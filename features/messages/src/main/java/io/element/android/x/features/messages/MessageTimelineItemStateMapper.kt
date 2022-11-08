package io.element.android.x.features.messages

import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.messages.model.MessagesItemGroupPosition
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimelineItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.MessageType

class MessageTimelineItemStateMapper(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun map(timelineItems: List<MatrixTimelineItem>): List<MessagesTimelineItemState> =
        withContext(dispatcher) {
            val messagesTimelineItemState = ArrayList<MessagesTimelineItemState>()
            for (index in timelineItems.indices.reversed()) {
                val currentTimelineItem = timelineItems[index]
                val timelineItemState = when (currentTimelineItem) {
                    is MatrixTimelineItem.Event -> {
                        buildMessageEvent(currentTimelineItem, index, timelineItems)
                    }
                    is MatrixTimelineItem.Virtual -> MessagesTimelineItemState.Virtual(
                        "virtual_item_$index"
                    )
                    MatrixTimelineItem.Other -> continue
                }
                messagesTimelineItemState.add(timelineItemState)
            }
            messagesTimelineItemState
        }

    private suspend fun buildMessageEvent(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>
    ): MessagesTimelineItemState.MessageEvent {
        val currentSender = currentTimelineItem.event.sender()
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderDisplayName = room.userDisplayName(currentSender).getOrNull()
        val senderAvatarUrl = room.userAvatarUrl(currentSender).getOrNull()
        val senderAvatarData =
            loadAvatarData(senderDisplayName ?: currentSender, senderAvatarUrl)

        return MessagesTimelineItemState.MessageEvent(
            id = currentTimelineItem.event.eventId() ?: "",
            senderId = currentSender,
            senderDisplayName = senderDisplayName,
            senderAvatar = senderAvatarData,
            content = currentTimelineItem.computeContent(),
            isMine = currentTimelineItem.event.isOwn(),
            groupPosition = groupPosition
        )
    }

    private fun MatrixTimelineItem.Event.computeContent(): String? {
        val messageType =
            event.content().asMessage()?.msgtype()
        return when (messageType) {
            is MessageType.Emote -> messageType.content.body
            is MessageType.Image -> messageType.content.body
            is MessageType.Notice -> messageType.content.body
            is MessageType.Text -> messageType.content.body
            null -> null
        }
    }

    private fun computeGroupPosition(
        currentTimelineItem: MatrixTimelineItem.Event,
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): MessagesItemGroupPosition {
        val prevTimelineItem =
            timelineItems.getOrNull(index - 1) as? MatrixTimelineItem.Event
        val nextTimelineItem =
            timelineItems.getOrNull(index + 1) as? MatrixTimelineItem.Event
        val currentSender = currentTimelineItem.event.sender()
        val previousSender = prevTimelineItem?.event?.sender()
        val nextSender = nextTimelineItem?.event?.sender()

        return when {
            previousSender != currentSender && nextSender == currentSender -> MessagesItemGroupPosition.First
            previousSender == currentSender && nextSender == currentSender -> MessagesItemGroupPosition.Middle
            previousSender == currentSender && nextSender != currentSender -> MessagesItemGroupPosition.Last
            else -> MessagesItemGroupPosition.None
        }
    }

    private suspend fun loadAvatarData(
        name: String,
        url: String?,
        size: AvatarSize = AvatarSize.SMALL
    ): AvatarData {
        val model = client.mediaResolver()
            .resolve(url, kind = MediaResolver.Kind.Thumbnail(size.value))
        return AvatarData(name, model, size)
    }


}