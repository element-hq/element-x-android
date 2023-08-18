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
import io.element.android.libraries.matrix.api.poll.isDisclosed
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import javax.inject.Inject

class TimelineItemContentPollFactory @Inject constructor(
    private val matrixClient: MatrixClient,
    private val featureFlagService: FeatureFlagService,
) {

    suspend fun create(content: PollContent): TimelineItemEventContent {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.Polls)) return TimelineItemUnknownContent

        // Todo Move this computation to the matrix rust sdk
        val pollVotesCount = content.votes.flatMap { it.value }.size
        val userVotes = content.votes.filter { matrixClient.sessionId in it.value }.keys
        val answerItems = content.answers.map { answer ->
            val votesCount = content.votes[answer.id]?.size ?: 0
            val isSelected = answer.id in userVotes
            val progress = when {
                pollVotesCount == 0 -> 0f
                content.kind.isDisclosed -> votesCount.toFloat() / pollVotesCount.toFloat()
                isSelected -> 1f
                else -> 0f
            }
            PollAnswerItem(
                answer = answer,
                isSelected = isSelected,
                isDisclosed = content.kind.isDisclosed,
                votesCount = votesCount,
                progress = progress,
            )
        }

        return TimelineItemPollContent(
            question = content.question,
            answerItems = answerItems,
            votes = content.votes,
            pollKind = content.kind,
        )
    }
}
