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
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

class FakeMatrixTimeline(
    initialTimelineItems: List<MatrixTimelineItem> = emptyList(),
    initialPaginationState: MatrixTimeline.PaginationState = MatrixTimeline.PaginationState(
        hasMoreToLoadBackwards = true,
        isBackPaginating = false,
        beginningOfRoomReached = false,
    )
) : MatrixTimeline {
    private val _paginationState: MutableStateFlow<MatrixTimeline.PaginationState> = MutableStateFlow(initialPaginationState)
    private val _timelineItems: MutableStateFlow<List<MatrixTimelineItem>> = MutableStateFlow(initialTimelineItems)

    var sendReadReceiptCount = 0
        private set

    var sendReadReceiptLatch: CompletableDeferred<Unit>? = null

    fun updatePaginationState(update: (MatrixTimeline.PaginationState.() -> MatrixTimeline.PaginationState)) {
        _paginationState.getAndUpdate(update)
    }

    fun updateTimelineItems(update: (items: List<MatrixTimelineItem>) -> List<MatrixTimelineItem>) {
        _timelineItems.getAndUpdate(update)
    }

    override val paginationState: StateFlow<MatrixTimeline.PaginationState> = _paginationState

    override val timelineItems: Flow<List<MatrixTimelineItem>> = _timelineItems

    override suspend fun paginateBackwards(requestSize: Int) = paginateBackwards()
    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int) = paginateBackwards()

    private suspend fun paginateBackwards(): Result<Unit> {
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

    override suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> = simulateLongTask {
        Result.success(Unit)
    }

    override suspend fun sendReadReceipt(
        eventId: EventId,
        receiptType: ReceiptType,
    ): Result<Unit> = simulateLongTask {
        sendReadReceiptCount++
        sendReadReceiptLatch?.complete(Unit)
        Result.success(Unit)
    }

    override fun close() = Unit
}
