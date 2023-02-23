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

package io.element.android.features.messages.timeline.factories.event

import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.core.EventId
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.matrix.timeline.MatrixTimelineItem
import kotlinx.collections.immutable.toImmutableList
import org.matrix.rustcomponents.sdk.ProfileTimelineDetails
import javax.inject.Inject

class TimelineItemEventFactory @Inject constructor(
    private val room: MatrixRoom,
    private val contentFactory: TimelineItemContentFactory,
) {

    fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
        index: Int,
        timelineItems: List<MatrixTimelineItem>,
    ): TimelineItem.Event {
        val currentSender = currentTimelineItem.event.sender()
        val groupPosition =
            computeGroupPosition(currentTimelineItem, timelineItems, index)
        val senderDisplayName: String?
        val senderAvatarUrl: String?

        when (val senderProfile = currentTimelineItem.event.senderProfile()) {
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

        val senderAvatarData = AvatarData(
            id = currentSender,
            name = senderDisplayName ?: currentSender,
            url = senderAvatarUrl,
            size = AvatarSize.SMALL
        )
        return TimelineItem.Event(
            id = EventId(currentTimelineItem.uniqueId),
            eventId = currentTimelineItem.eventId,
            senderId = currentSender,
            senderDisplayName = senderDisplayName,
            senderAvatar = senderAvatarData,
            content = contentFactory.create(currentTimelineItem.event.content()),
            isMine = currentTimelineItem.event.isOwn(),
            groupPosition = groupPosition,
            reactionsState = currentTimelineItem.computeReactionsState()
        )
    }

    private fun MatrixTimelineItem.Event.computeReactionsState(): TimelineItemReactions {
        val aggregatedReactions = event.reactions().orEmpty().map {
            AggregatedReaction(key = it.key, count = it.count.toString(), isHighlighted = false)
        }
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
        val currentSender = currentTimelineItem.event.sender()
        val previousSender = prevTimelineItem?.event?.sender()
        val nextSender = nextTimelineItem?.event?.sender()

        return when {
            previousSender != currentSender && nextSender == currentSender -> TimelineItemGroupPosition.First
            previousSender == currentSender && nextSender == currentSender -> TimelineItemGroupPosition.Middle
            previousSender == currentSender && nextSender != currentSender -> TimelineItemGroupPosition.Last
            else -> TimelineItemGroupPosition.None
        }
    }
}
