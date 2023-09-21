/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.groups.canBeDisplayedInBubbleBlock
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionSender
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import kotlinx.collections.immutable.toImmutableList
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

class TimelineItemEventFactory @Inject constructor(
    private val contentFactory: TimelineItemContentFactory,
    private val matrixClient: MatrixClient,
) {

    suspend fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
    ): TimelineItem.Event {
        val currentSender = currentTimelineItem.event.sender
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderDisplayName: String?
        val senderAvatarUrl: String?

        when (val senderProfile = currentTimelineItem.event.senderProfile) {
            ProfileTimelineDetails.Unavailable,
            ProfileTimelineDetails.Pending,
            is ProfileTimelineDetails.Error -> {
                senderDisplayName = null
                senderAvatarUrl = null
            }
            is ProfileTimelineDetails.Ready -> {
                senderDisplayName = senderProfile.displayName
                senderAvatarUrl = senderProfile.avatarUrl
            }
        }

        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
        val sentTime = timeFormatter.format(Date(currentTimelineItem.event.timestamp))

        val senderAvatarData = AvatarData(
            id = currentSender.value,
            name = senderDisplayName ?: currentSender.value,
            url = senderAvatarUrl,
            size = AvatarSize.TimelineSender
        )
        currentTimelineItem.event
        return TimelineItem.Event(
            id = currentTimelineItem.uniqueId.toString(),
            eventId = currentTimelineItem.eventId,
            transactionId = currentTimelineItem.transactionId,
            senderId = currentSender,
            senderDisplayName = senderDisplayName,
            senderAvatar = senderAvatarData,
            content = contentFactory.create(currentTimelineItem.event),
            isMine = currentTimelineItem.event.isOwn,
            sentTime = sentTime,
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState(),
            localSendState = currentTimelineItem.event.localSendState,
            inReplyTo = currentTimelineItem.event.inReplyTo(),
            isThreaded = currentTimelineItem.event.isThreaded(),
            debugInfo = currentTimelineItem.event.debugInfo,
            origin = currentTimelineItem.event.origin,
        )
    }

    private fun MatrixTimelineItem.Event.computeReactionsState(): TimelineItemReactions {
        val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
        var aggregatedReactions = event.reactions.map { reaction ->
            // Sort reactions within an aggregation by timestamp descending.
            // This puts the most recent at the top, useful in cases like the
            // reaction summary view or getting the most recent reaction.
            AggregatedReaction(
                key = reaction.key,
                currentUserId = matrixClient.sessionId,
                senders = reaction.senders
                    .sortedByDescending{ it.timestamp }
                    .map {
                        val date = Date(it.timestamp)
                        AggregatedReactionSender(
                            senderId = it.senderId,
                            timestamp = date,
                            sentTime = timeFormatter.format(date),
                        )
                    }
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
        return TimelineItemReactions(aggregatedReactions.toImmutableList())
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
            previousSender == currentSender /* && nextSender != currentSender (== true) */ -> {
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
