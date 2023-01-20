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

package io.element.android.libraries.matrix.timeline

import io.element.android.libraries.matrix.core.EventId
import kotlinx.coroutines.flow.Flow
import org.matrix.rustcomponents.sdk.TimelineListener

interface MatrixTimeline {
    var callback: Callback?
    val hasMoreToLoad: Boolean

    interface Callback {
        fun onUpdatedTimelineItem(timelineItem: MatrixTimelineItem) = Unit
        fun onPushedTimelineItem(timelineItem: MatrixTimelineItem) = Unit
    }

    fun timelineItems(): Flow<List<MatrixTimelineItem>>
    suspend fun paginateBackwards(count: Int): Result<Unit>
    fun addListener(timelineListener: TimelineListener)
    fun initialize()
    fun dispose()

    /**
     * @param message markdown message
     */
    suspend fun sendMessage(message: String): Result<Unit>

    suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit>

    suspend fun replyMessage(inReplyToEventId: EventId, message: String): Result<Unit>
}
