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
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.api.timeline.getActiveTimeline
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@AssistedInject
class PollRepository(
    private val room: JoinedRoom,
    private val defaultTimelineProvider: TimelineProvider,
    @Assisted private val timelineMode: Timeline.Mode,
) {
    @AssistedFactory
    fun interface Factory {
        fun create(
            timelineMode: Timeline.Mode,
        ): PollRepository
    }

    suspend fun getPoll(eventId: EventId): Result<PollContent> = runCatchingExceptions {
        getTimelineProvider()
            .getOrThrow()
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
        null -> getTimelineProvider().flatMap { timelineProvider ->
            timelineProvider
                .getActiveTimeline()
                .createPoll(
                    question = question,
                    answers = answers,
                    maxSelections = maxSelections,
                    pollKind = pollKind,
                )
        }
        else -> getTimelineProvider().flatMap { timelineProvider ->
            timelineProvider.getActiveTimeline()
                .editPoll(
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
    ): Result<Unit> =
        getTimelineProvider().flatMap { timelineProvider ->
            timelineProvider.getActiveTimeline()
                .redactEvent(
                    eventOrTransactionId = pollStartId.toEventOrTransactionId(),
                    reason = null,
                )
        }

    private suspend fun getTimelineProvider(): Result<TimelineProvider> {
        return when (timelineMode) {
            is Timeline.Mode.Thread -> {
                val threadedTimelineResult = room.createTimeline(CreateTimelineParams.Threaded(timelineMode.threadRootId))
                threadedTimelineResult.map { threadedTimeline ->
                    object : TimelineProvider {
                        private val flow = MutableStateFlow<Timeline?>(threadedTimeline)
                        override fun activeTimelineFlow(): StateFlow<Timeline?> = flow
                    }
                }
            }
            else -> Result.success(defaultTimelineProvider)
        }
    }
}
