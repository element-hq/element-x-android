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

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.poll.api.PollAnswerItem
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.isDisclosed
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import javax.inject.Inject

class TimelineItemContentPollFactory @Inject constructor(
    private val matrixClient: MatrixClient,
    private val featureFlagService: FeatureFlagService,
) {

    suspend fun create(
        content: PollContent,
        eventId: EventId?
    ): TimelineItemEventContent {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.Polls)) return TimelineItemUnknownContent

        // Todo Move this computation to the matrix rust sdk
        val totalVoteCount = content.votes.flatMap { it.value }.size
        val myVotes = content.votes.filter { matrixClient.sessionId in it.value }.keys
        val isEndedPoll = content.endTime != null
        val winnerIds = if (!isEndedPoll) {
            emptyList()
        } else {
            content.answers
                .map { answer -> answer.id }
                .groupBy { answerId -> content.votes[answerId]?.size ?: 0 } // Group by votes count
                .maxByOrNull { (votes, _) -> votes } // Keep max voted answers
                ?.takeIf { (votes, _) -> votes > 0 } // Ignore if no option has been voted
                ?.value
                .orEmpty()
        }
        val answerItems = content.answers.map { answer ->
            val answerVoteCount = content.votes[answer.id]?.size ?: 0
            val isSelected = answer.id in myVotes
            val isWinner = answer.id in winnerIds
            val percentage = if (totalVoteCount > 0) answerVoteCount.toFloat() / totalVoteCount.toFloat() else 0f
            PollAnswerItem(
                answer = answer,
                isSelected = isSelected,
                isEnabled = !isEndedPoll,
                isWinner = isWinner,
                isDisclosed = content.kind.isDisclosed || isEndedPoll,
                votesCount = answerVoteCount,
                percentage = percentage,
            )
        }

        return TimelineItemPollContent(
            eventId = eventId,
            question = content.question,
            answerItems = answerItems,
            pollKind = content.kind,
            isEnded = isEndedPoll,
        )
    }
}
