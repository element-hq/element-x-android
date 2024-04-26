/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.timeline

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.LiveTimelineProvider
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
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
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.Closeable
import java.util.Optional
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * This controller is responsible of using the right timeline to display messages and make associated actions.
 * It can be focused on the live timeline or on a detached timeline (focusing an unknown event).
 * This controller will replace the [LiveTimelineProvider] in the DI.
 */
@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class, boundType = TimelineProvider::class, replaces = [LiveTimelineProvider::class])
class TimelineController @Inject constructor(
    private val room: MatrixRoom,
) : Closeable, TimelineProvider {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val liveTimeline = MutableStateFlow(room.liveTimeline)
    private val detachedTimeline = MutableStateFlow<Optional<Timeline>>(Optional.empty())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return currentTimelineFlow.flatMapLatest { it.timelineItems }
    }

    fun isLive(): Flow<Boolean> {
        return detachedTimeline.map { !it.isPresent }
    }

    suspend fun invokeOnCurrentTimeline(block: suspend (Timeline.() -> Any)) {
        currentTimelineFlow.value.run {
            block(this)
        }
    }

    suspend fun focusOnEvent(eventId: EventId): Result<Unit> {
        return try {
            val newDetachedTimeline = room.timelineFocusedOnEvent(eventId)
            detachedTimeline.getAndUpdate { current ->
                if (current.isPresent) {
                    current.get().close()
                }
                Optional.of(newDetachedTimeline)
            }
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            Result.failure(exception)
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
        detachedTimeline.getAndUpdate {
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

    private val currentTimelineFlow = combine(liveTimeline, detachedTimeline) { live, detached ->
        when {
            detached.isPresent -> detached.get()
            else -> live
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, room.liveTimeline)

    override fun activeTimelineFlow(): StateFlow<Timeline> {
        return currentTimelineFlow
    }
}
