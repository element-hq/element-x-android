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

package io.element.android.features.poll.impl.data

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.api.timeline.getActiveTimeline
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PollRepository @Inject constructor(
    private val room: MatrixRoom,
    private val timelineProvider: TimelineProvider,
) {
    suspend fun getPoll(eventId: EventId): Result<PollContent> = runCatching {
        timelineProvider
            .getActiveTimeline()
            .timelineItems
            .first()
            .asSequence()
            .filterIsInstance<MatrixTimelineItem.Event>()
            .first { it.eventId == eventId }
            .event
            .content as PollContent
    }

    suspend fun savePoll(
        existingPollId: EventId?,
        question: String,
        answers: List<String>,
        pollKind: PollKind,
        maxSelections: Int,
    ): Result<Unit> = when (existingPollId) {
        null -> room.createPoll(
            question = question,
            answers = answers,
            maxSelections = maxSelections,
            pollKind = pollKind,
        )
        else -> timelineProvider
            .getActiveTimeline()
            .editPoll(
                pollStartId = existingPollId,
                question = question,
                answers = answers,
                maxSelections = maxSelections,
                pollKind = pollKind,
            )
    }

    suspend fun deletePoll(
        pollStartId: EventId,
    ): Result<Unit> =
        room.redactEvent(
            eventId = pollStartId,
        )
}
