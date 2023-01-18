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

package io.element.android.x.matrix.timeline

import io.element.android.x.matrix.core.EventId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.matrix.rustcomponents.sdk.TimelineListener

class FakeMatrixTimeline : MatrixTimeline {

    override var callback: MatrixTimeline.Callback?
        get() = TODO("Not yet implemented")
        set(value) {}

    override val hasMoreToLoad: Boolean
        get() = true

    override fun timelineItems(): Flow<List<MatrixTimelineItem>> {
        return emptyFlow()
    }

    override suspend fun paginateBackwards(count: Int): Result<Unit> {
        return Result.success(Unit)
    }

    override fun addListener(timelineListener: TimelineListener) {
        //
    }

    override fun initialize() = Unit

    override fun dispose() = Unit

    override suspend fun sendMessage(message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun replyMessage(inReplyToEventId: EventId, message: String): Result<Unit> {
        return Result.success(Unit)
    }
}
