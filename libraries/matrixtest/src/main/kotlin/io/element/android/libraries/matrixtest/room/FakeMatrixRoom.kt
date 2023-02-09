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

package io.element.android.libraries.matrixtest.room

import io.element.android.libraries.matrix.core.EventId
import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.matrix.timeline.MatrixTimeline
import io.element.android.libraries.matrixtest.timeline.FakeMatrixTimeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeMatrixRoom(
    override val roomId: RoomId,
    override val name: String? = null,
    override val bestName: String = "",
    override val displayName: String = "",
    override val topic: String? = null,
    override val avatarUrl: String? = null,
    private val matrixTimeline: MatrixTimeline = FakeMatrixTimeline(),
) : MatrixRoom {

    override fun syncUpdateFlow(): Flow<Long> {
        return emptyFlow()
    }

    override fun timeline(): MatrixTimeline {
        return matrixTimeline
    }

    override suspend fun userDisplayName(userId: String): Result<String?> {
        return Result.success("")
    }

    override suspend fun userAvatarUrl(userId: String): Result<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: String): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    var editMessageParameter: String? = null
        private set

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> {
        editMessageParameter = message
        delay(100)
        return Result.success(Unit)
    }

    var replyMessageParameter: String? = null
        private set

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> {
        replyMessageParameter = message
        delay(100)
        return Result.success(Unit)
    }

    override suspend fun redactEvent(eventId: EventId, reason: String?): Result<Unit> {
        TODO("Not yet implemented")
    }
}
