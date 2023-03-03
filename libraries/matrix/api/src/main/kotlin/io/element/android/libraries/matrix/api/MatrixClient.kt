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

package io.element.android.libraries.matrix.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.media.MediaResolver
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import org.matrix.rustcomponents.sdk.MediaSource
import java.io.Closeable

interface MatrixClient : Closeable {
    val sessionId: SessionId
    fun getRoom(roomId: RoomId): MatrixRoom?
    fun startSync()
    fun stopSync()
    fun roomSummaryDataSource(): RoomSummaryDataSource
    fun mediaResolver(): MediaResolver
    suspend fun logout()
    suspend fun loadUserDisplayName(): Result<String>
    suspend fun loadUserAvatarURLString(): Result<String>
    suspend fun loadMediaContentForSource(source: MediaSource): Result<ByteArray>
    suspend fun loadMediaThumbnailForSource(
        source: MediaSource,
        width: Long,
        height: Long
    ): Result<ByteArray>
}
