package io.element.android.x.features.messages

import io.element.android.x.features.messages.model.MessagesItemGroupPosition
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.matrix.core.UserId
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.matrix.timeline.MatrixTimelineItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.MessageType

class MessageTimelineItemStateMapper(
    private val myUserId: UserId,
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
                        val prevTimelineItem =
                            timelineItems.getOrNull(index - 1) as? MatrixTimelineItem.Event
                        val nextTimelineItem =
                            timelineItems.getOrNull(index + 1) as? MatrixTimelineItem.Event
                        val currentSender = currentTimelineItem.event.sender()
                        val previousSender = prevTimelineItem?.event?.sender()
                        val nextSender = nextTimelineItem?.event?.sender()

                        val groupPosition = when {
                            previousSender != currentSender && nextSender == currentSender -> MessagesItemGroupPosition.First
                            previousSender == currentSender && nextSender == currentSender -> MessagesItemGroupPosition.Middle
                            previousSender == currentSender && nextSender != currentSender -> MessagesItemGroupPosition.Last
                            else -> MessagesItemGroupPosition.None
                        }
                        val messageType =
                            currentTimelineItem.event.content().asMessage()?.msgtype()
                        val contentStr = when (messageType) {
                            is MessageType.Emote -> messageType.content.body
                            is MessageType.Image -> messageType.content.body
                            is MessageType.Notice -> messageType.content.body
                            is MessageType.Text -> messageType.content.body
                            null -> null
                        }
                        MessagesTimelineItemState.MessageEvent(
                            id = currentTimelineItem.event.eventId() ?: "",
                            sender = currentTimelineItem.event.sender(),
                            content = contentStr,
                            isMine = currentTimelineItem.event.isOwn(),
                            groupPosition = groupPosition
                        )
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


}