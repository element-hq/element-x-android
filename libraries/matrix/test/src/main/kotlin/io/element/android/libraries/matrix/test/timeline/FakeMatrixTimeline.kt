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

package io.element.android.libraries.matrix.test.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeMatrixTimeline(
    initialTimelineItems: List<MatrixTimelineItem> = emptyList(),
    initialPaginationState: MatrixTimeline.PaginationState = MatrixTimeline.PaginationState(canBackPaginate = true, isBackPaginating = false)
) : MatrixTimeline {

    private val paginationState: MutableStateFlow<MatrixTimeline.PaginationState> = MutableStateFlow(initialPaginationState)
    private val timelineItems: MutableStateFlow<List<MatrixTimelineItem>> = MutableStateFlow(initialTimelineItems)
    var isInitialized = false

    fun updatePaginationState(update: (MatrixTimeline.PaginationState.() -> MatrixTimeline.PaginationState)) {
        paginationState.value = update(paginationState.value)
    }

    fun updateTimelineItems(update: (items: List<MatrixTimelineItem>) -> List<MatrixTimelineItem>) {
        timelineItems.value = update(timelineItems.value)
    }

    override fun paginationState(): StateFlow<MatrixTimeline.PaginationState> {
        return paginationState
    }

    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return timelineItems
    }

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> {
        updatePaginationState {
            copy(isBackPaginating = true)
        }
        delay(100)
        updatePaginationState {
            copy(isBackPaginating = false)
        }
        updateTimelineItems { timelineItems ->
            timelineItems
        }
        return Result.success(Unit)
    }

    override fun initialize() {
        isInitialized = true
    }

    override fun dispose() {
        isInitialized = false
    }

    override suspend fun sendMessage(message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun replyMessage(inReplyToEventId: EventId, message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> {
        return Result.success(Unit)
    }
}
