/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import io.element.android.features.messages.impl.timeline.groups.canBeDisplayedInBubbleBlock
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionSender
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.messages.reply.map
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.text.DateFormat
import java.util.Date

class TimelineItemEventFactory @AssistedInject constructor(
    @Assisted private val config: TimelineItemsFactoryConfig,
    private val contentFactory: TimelineItemContentFactory,
    private val matrixClient: MatrixClient,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val permalinkParser: PermalinkParser,
) {
    @AssistedFactory
    interface Creator {
        fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory
    }

    suspend fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
        roomMembers: List<RoomMember>,
    ): TimelineItem.Event {
        val currentSender = currentTimelineItem.event.sender
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderProfile = currentTimelineItem.event.senderProfile
        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
        val sentTime = timeFormatter.format(Date(currentTimelineItem.event.timestamp))

        val senderAvatarData = AvatarData(
            id = currentSender.value,
            name = senderProfile.getDisambiguatedDisplayName(currentSender),
            url = senderProfile.getAvatarUrl(),
            size = AvatarSize.TimelineSender
        )
        currentTimelineItem.event
        return TimelineItem.Event(
            id = currentTimelineItem.uniqueId,
            eventId = currentTimelineItem.eventId,
            transactionId = currentTimelineItem.transactionId,
            senderId = currentSender,
            senderProfile = senderProfile,
            senderAvatar = senderAvatarData,
            content = contentFactory.create(currentTimelineItem.event),
            isMine = currentTimelineItem.event.isOwn,
            isEditable = currentTimelineItem.event.isEditable,
            canBeRepliedTo = currentTimelineItem.event.canBeRepliedTo,
            sentTime = sentTime,
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState(),
            readReceiptState = currentTimelineItem.computeReadReceiptState(roomMembers),
            localSendState = currentTimelineItem.event.localSendState,
            inReplyTo = currentTimelineItem.event.inReplyTo()?.map(permalinkParser = permalinkParser),
            isThreaded = currentTimelineItem.event.isThreaded(),
            origin = currentTimelineItem.event.origin,
            timelineItemDebugInfoProvider = currentTimelineItem.event.timelineItemDebugInfoProvider,
            messageShieldProvider = currentTimelineItem.event.messageShieldProvider,
        )
    }

    fun update(
        timelineItem: TimelineItem.Event,
        receivedMatrixTimelineItem: MatrixTimelineItem.Event,
        roomMembers: List<RoomMember>,
    ): TimelineItem.Event {
        return timelineItem.copy(
            readReceiptState = receivedMatrixTimelineItem.computeReadReceiptState(roomMembers)
        )
    }

    private fun MatrixTimelineItem.Event.computeReactionsState(): TimelineItemReactions {
        if (!config.computeReactions) {
            return TimelineItemReactions(reactions = persistentListOf())
        }
        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
        var aggregatedReactions = this.event.reactions.map { reaction ->
            // Sort reactions within an aggregation by timestamp descending.
            // This puts the most recent at the top, useful in cases like the
            // reaction summary view or getting the most recent reaction.
            AggregatedReaction(
                key = reaction.key,
                currentUserId = matrixClient.sessionId,
                senders = reaction.senders
                    .sortedByDescending { it.timestamp }
                    .map {
                        val date = Date(it.timestamp)
                        AggregatedReactionSender(
                            senderId = it.senderId,
                            timestamp = date,
                            sentTime = timeFormatter.format(date),
                        )
                    }
                    .toImmutableList()
            )
        }
        // Sort aggregated reactions by count and then timestamp ascending, using
        // the most recent reaction in the aggregation(hence index 0).
        // This appends new aggregations on the end of the reaction layout.
        aggregatedReactions = aggregatedReactions
            .sortedWith(
                compareByDescending<AggregatedReaction> { it.count }
                    .thenBy { it.senders[0].timestamp }
            )
        return TimelineItemReactions(
            reactions = aggregatedReactions.toImmutableList()
        )
    }

    private fun MatrixTimelineItem.Event.computeReadReceiptState(
        roomMembers: List<RoomMember>,
    ): TimelineItemReadReceipts {
        if (!config.computeReadReceipts) {
            return TimelineItemReadReceipts(receipts = persistentListOf())
        }
        return TimelineItemReadReceipts(
            receipts = event.receipts
                .map { receipt ->
                    val roomMember = roomMembers.find { it.userId == receipt.userId }
                    ReadReceiptData(
                        avatarData = AvatarData(
                            id = receipt.userId.value,
                            name = roomMember?.displayName,
                            url = roomMember?.avatarUrl,
                            size = AvatarSize.TimelineReadReceipt,
                        ),
                        formattedDate = lastMessageTimestampFormatter.format(receipt.timestamp)
                    )
                }
                .toImmutableList()
        )
    }

    private fun computeGroupPosition(
        currentTimelineItem: MatrixTimelineItem.Event,
        timelineItems: List<MatrixTimelineItem>,
        index: Int
    ): TimelineItemGroupPosition {
        val prevTimelineItem =
            timelineItems.getOrNull(index - 1) as? MatrixTimelineItem.Event
        val nextTimelineItem =
            timelineItems.getOrNull(index + 1) as? MatrixTimelineItem.Event
        val currentSender = currentTimelineItem.event.sender
        val previousSender = prevTimelineItem?.event?.sender
        val nextSender = nextTimelineItem?.event?.sender

        val previousIsGroupable = prevTimelineItem?.canBeDisplayedInBubbleBlock().orTrue()
        val nextIsGroupable = nextTimelineItem?.canBeDisplayedInBubbleBlock().orTrue()

        return when {
            previousSender != currentSender && nextSender == currentSender -> {
                if (nextIsGroupable) {
                    TimelineItemGroupPosition.First
                } else {
                    TimelineItemGroupPosition.None
                }
            }
            previousSender == currentSender && nextSender == currentSender -> {
                if (previousIsGroupable) {
                    if (nextIsGroupable) {
                        TimelineItemGroupPosition.Middle
                    } else {
                        TimelineItemGroupPosition.Last
                    }
                } else {
                    if (nextIsGroupable) {
                        TimelineItemGroupPosition.First
                    } else {
                        TimelineItemGroupPosition.None
                    }
                }
            }
            // In the following case, we have nextSender != currentSender == true
            previousSender == currentSender -> {
                if (previousIsGroupable) {
                    TimelineItemGroupPosition.Last
                } else {
                    TimelineItemGroupPosition.None
                }
            }
            else -> TimelineItemGroupPosition.None
        }
    }
}
