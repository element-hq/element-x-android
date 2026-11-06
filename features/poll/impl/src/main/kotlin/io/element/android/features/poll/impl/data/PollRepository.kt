/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.data

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import kotlinx.coroutines.flow.first

@AssistedInject
class PollRepository(
    private val room: JoinedRoom,
    @Assisted private val timelineMode: Timeline.Mode,
) {
    @AssistedFactory
    fun interface Factory {
        fun create(
            timelineMode: Timeline.Mode,
        ): PollRepository
    }

    suspend fun getPoll(eventId: EventId): Result<PollContent> = withTimeline(focusedEventId = eventId) { timeline ->
        runCatchingExceptions {
            timeline.timelineItems
                .first()
                .asSequence()
                .filterIsInstance<MatrixTimelineItem.Event>()
                .first { it.eventId == eventId }
                .event
                .content as PollContent
        }
    }

    suspend fun savePoll(
        existingPollId: EventId?,
        question: String,
        answers: List<String>,
        pollKind: PollKind,
        maxSelections: Int,
    ): Result<Unit> = withTimeline(focusedEventId = existingPollId) { timeline ->
        when (existingPollId) {
            null -> timeline.createPoll(
                question = question,
                answers = answers,
                maxSelections = maxSelections,
                pollKind = pollKind,
            )
            else -> timeline.editPoll(
                pollStartId = existingPollId,
                question = question,
                answers = answers,
                maxSelections = maxSelections,
                pollKind = pollKind,
            )
        }
    }

    suspend fun deletePoll(
        pollStartId: EventId,
    ): Result<Unit> = withTimeline(focusedEventId = pollStartId) { timeline ->
        timeline.redactEvent(
            eventOrTransactionId = pollStartId.toEventOrTransactionId(),
            reason = null,
        )
    }

    /**
     * Invokes [block] with the [Timeline] matching [timelineMode].
     *
     * For a thread, or when acting on the existing poll [focusedEventId] outside a thread, a dedicated
     * timeline is created and closed once [block] returns, since such a timeline owns SDK resources.
     * Otherwise, the live timeline of the room is used, and is not closed here since it is owned by the room.
     */
    private suspend fun <T> withTimeline(
        focusedEventId: EventId?,
        block: suspend (Timeline) -> Result<T>,
    ): Result<T> {
        val createTimelineParams = when {
            timelineMode is Timeline.Mode.Thread -> CreateTimelineParams.Threaded(timelineMode.threadRootId)
            focusedEventId != null -> CreateTimelineParams.Focused(focusedEventId)
            else -> return block(room.liveTimeline)
        }
        return room.createTimeline(createTimelineParams)
            .flatMap { timeline ->
                timeline.use { block(it) }
            }
    }
}
