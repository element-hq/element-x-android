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

package io.element.android.libraries.matrix.api.room

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.Duration

interface RoomSummaryDataSource {

    sealed class LoadingState {
        object NotLoaded : LoadingState()
        data class Loaded(val numberOfRooms: Int) : LoadingState()
    }

    fun updateAllRoomsVisibleRange(range: IntRange)
    fun allRoomsLoadingState(): StateFlow<LoadingState>
    fun allRooms(): StateFlow<List<RoomSummary>>
    fun inviteRooms(): StateFlow<List<RoomSummary>>
}

suspend fun RoomSummaryDataSource.awaitAllRoomsAreLoaded(timeout: Duration = Duration.INFINITE) {
    try {
        withTimeout(timeout) {
            allRoomsLoadingState().firstOrNull {
                it is RoomSummaryDataSource.LoadingState.Loaded
            }
        }
    } catch (timeoutException: TimeoutCancellationException) {
        Timber.v("AwaitAllRooms: no response after $timeout")
    }
}
