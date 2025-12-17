/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import io.element.android.features.messages.impl.timeline.groups.canBeDisplayedInBubbleBlock
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionSender
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatter
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.matrix.ui.messages.reply.map
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@AssistedInject
class TimelineItemEventFactory(
    @Assisted private val config: TimelineItemsFactoryConfig,
    private val contentFactory: TimelineItemContentFactory,
    private val matrixClient: MatrixClient,
    private val dateFormatter: DateFormatter,
    private val permalinkParser: PermalinkParser,
    private val summaryFormatter: MessageSummaryFormatter,
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
        val sentTime = dateFormatter.format(
            timestamp = currentTimelineItem.event.timestamp,
            mode = DateFormatterMode.TimeOnly,
        )
        val senderAvatarData = AvatarData(
            id = currentSender.value,
            name = senderProfile.getDisambiguatedDisplayName(currentSender),
            url = senderProfile.getAvatarUrl(),
            size = AvatarSize.TimelineSender
        )
        val mappedThreadInfo = when (val threadInfo = currentTimelineItem.event.threadInfo()) {
            is EventThreadInfo.ThreadResponse -> {
                TimelineItemThreadInfo.ThreadResponse(threadInfo.threadRootId)
            }
            is EventThreadInfo.ThreadRoot -> {
                TimelineItemThreadInfo.ThreadRoot(
                    summary = threadInfo.summary,
                    latestEventText = threadInfo.summary.latestEvent.dataOrNull()
                        ?.let {
                            contentFactory.create(
                                itemContent = it.content,
                                eventId = it.eventOrTransactionId.eventId,
                                isEditable = false,
                                sender = it.senderId,
                                senderProfile = it.senderProfile,
                            )
                        }
                        ?.let(summaryFormatter::format)
                )
            }
            null -> null
        }

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
            sentTimeMillis = currentTimelineItem.event.timestamp,
            sentTime = sentTime,
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState(),
            readReceiptState = currentTimelineItem.computeReadReceiptState(roomMembers),
            localSendState = currentTimelineItem.event.localSendState,
            inReplyTo = currentTimelineItem.event.inReplyTo()?.map(permalinkParser = permalinkParser),
            threadInfo = mappedThreadInfo,
            origin = currentTimelineItem.event.origin,
            timelineItemDebugInfoProvider = currentTimelineItem.event.timelineItemDebugInfoProvider,
            messageShieldProvider = currentTimelineItem.event.messageShieldProvider,
            sendHandleProvider = currentTimelineItem.event.sendHandleProvider,
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
                        AggregatedReactionSender(
                            senderId = it.senderId,
                            timestamp = it.timestamp,
                            sentTime = dateFormatter.format(
                                it.timestamp,
                                DateFormatterMode.TimeOrDate,
                            ),
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
                        formattedDate = dateFormatter.format(
                            receipt.timestamp,
                            mode = DateFormatterMode.TimeOrDate,
                        )
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
