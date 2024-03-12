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

package io.element.android.libraries.matrix.api.timeline

import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MatrixTimeline : AutoCloseable {
    data class PaginationState(
        val isBackPaginating: Boolean,
        val hasMoreToLoadBackwards: Boolean,
        val beginningOfRoomReached: Boolean,
    ) {
        val canBackPaginate = !isBackPaginating && hasMoreToLoadBackwards

        companion object {
            val Initial = PaginationState(
                isBackPaginating = false,
                hasMoreToLoadBackwards = true,
                beginningOfRoomReached = false
            )
        }
    }

    val paginationState: StateFlow<PaginationState>
    val timelineItems: Flow<List<MatrixTimelineItem>>
    val membershipChangeEventReceived: Flow<Unit>

    suspend fun paginateBackwards(requestSize: Int): Result<Unit>
    suspend fun paginateBackwards(requestSize: Int, untilNumberOfItems: Int): Result<Unit>
    suspend fun fetchDetailsForEvent(eventId: EventId): Result<Unit>
    suspend fun sendReadReceipt(eventId: EventId, receiptType: ReceiptType): Result<Unit>
}
