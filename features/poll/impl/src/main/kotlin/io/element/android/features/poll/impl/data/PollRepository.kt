/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.data

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.api.timeline.getActiveTimeline
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
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
        timelineProvider
            .getActiveTimeline()
            .redactEvent(
                eventOrTransactionId = pollStartId.toEventOrTransactionId(),
                reason = null,
            )
}
