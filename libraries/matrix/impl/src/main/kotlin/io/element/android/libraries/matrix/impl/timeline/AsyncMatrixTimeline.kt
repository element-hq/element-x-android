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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * This class is a wrapper around a [MatrixTimeline] that will be created asynchronously.
 */
@Suppress("unused")
class AsyncMatrixTimeline(
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    private val timelineProvider: suspend () -> MatrixTimeline
) : MatrixTimeline {
    private val _timelineItems: MutableStateFlow<List<MatrixTimelineItem>> =
        MutableStateFlow(emptyList())

    private val _paginationState = MutableStateFlow(
        MatrixTimeline.PaginationState.Initial
    )
    private val timeline = coroutineScope.async(context = dispatcher, start = CoroutineStart.LAZY) {
        timelineProvider()
    }
    private val closeSignal = CompletableDeferred<Unit>()

    override val membershipChangeEventReceived = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        coroutineScope.launch {
            val delegateTimeline = timeline.await()
            delegateTimeline.timelineItems
                .onEach { _timelineItems.value = it }
                .launchIn(this)
            delegateTimeline.paginationState
                .onEach { _paginationState.value = it }
                .launchIn(this)
            delegateTimeline.membershipChangeEventReceived
                .onEach { membershipChangeEventReceived.emit(it) }
                .launchIn(this)

            launch {
                withContext(NonCancellable) {
                    closeSignal.await()
                    Timber.d("Close delegate")
                    delegateTimeline.close()
                }
            }
        }
    }

    override val paginationState: StateFlow<MatrixTimeline.PaginationState> = _paginationState
    override val timelineItems: Flow<List<MatrixTimelineItem>> = _timelineItems

    override suspend fun paginateBackwards(requestSize: Int): Result<Unit> {
        return timeline.await().paginateBackwards(requestSize)
    }

    override suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit> {
        return timeline.await().paginateBackwards(requestSize, untilNumberOfItems)
    }

    override suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit> {
        return timeline.await().fetchDetailsForEvent(eventId)
    }

    override suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit> {
        return timeline.await().sendReadReceipt(eventId, receiptType)
    }

    override fun close() {
        closeSignal.complete(Unit)
    }
}
