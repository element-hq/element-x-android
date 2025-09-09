/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.features.messages.impl.timeline.di.LiveTimeline
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.Closeable
import java.util.Optional

/**
 * This controller is responsible of using the right timeline to display messages and make associated actions.
 * It can be focused on the live timeline or on a detached timeline (focusing an unknown event).
 */
@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class, binding = binding<TimelineProvider>())
@Inject class TimelineController(
    private val room: JoinedRoom,
    @LiveTimeline private val liveTimeline: Timeline,
) : Closeable, TimelineProvider {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val liveTimelineFlow = flowOf(liveTimeline)
    private val detachedTimelineFlow = MutableStateFlow<Optional<Timeline>>(Optional.empty())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return currentTimelineFlow.flatMapLatest { it.timelineItems }
    }

    fun isLive(): Flow<Boolean> {
        return detachedTimelineFlow.map { !it.isPresent }
    }

    fun mainTimelineMode(): Timeline.Mode = liveTimeline.mode

    fun detachedTimelineMode(): Timeline.Mode? {
        return detachedTimelineFlow.value.orElse(null)?.mode
    }

    suspend fun invokeOnCurrentTimeline(block: suspend (Timeline.() -> Unit)) {
        currentTimelineFlow.value.run {
            block(this)
        }
    }

    suspend fun focusOnEvent(eventId: EventId): Result<Unit> {
        return room.createTimeline(CreateTimelineParams.Focused(eventId))
            .onFailure {
                if (it is CancellationException) {
                    throw it
                }
            }
            .map { newDetachedTimeline ->
                detachedTimelineFlow.getAndUpdate { current ->
                    if (current.isPresent) {
                        current.get().close()
                    }
                    Optional.of(newDetachedTimeline)
                }
            }
    }

    /**
     * Makes sure the controller is focused on the live timeline.
     * This does close the detached timeline if any.
     */
    fun focusOnLive() {
        closeDetachedTimeline()
    }

    private fun closeDetachedTimeline() {
        detachedTimelineFlow.getAndUpdate {
            when {
                it.isPresent -> {
                    it.get().close()
                    Optional.empty()
                }
                else -> Optional.empty()
            }
        }
    }

    override fun close() {
        coroutineScope.cancel()
        closeDetachedTimeline()
    }

    suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> {
        return currentTimelineFlow.value.paginate(direction)
            .onSuccess { hasReachedEnd ->
                if (direction == Timeline.PaginationDirection.FORWARDS && hasReachedEnd) {
                    focusOnLive()
                }
            }
    }

    private val currentTimelineFlow = combine(liveTimelineFlow, detachedTimelineFlow) { live, detached ->
        when {
            detached.isPresent -> detached.get()
            else -> live
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, room.liveTimeline)

    override fun activeTimelineFlow(): StateFlow<Timeline> {
        return currentTimelineFlow
    }
}
