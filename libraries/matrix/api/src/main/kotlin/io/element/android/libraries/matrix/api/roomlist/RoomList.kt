/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.Duration

/**
 * Holds some flows related to a specific set of rooms.
 * Can be retrieved from [RoomListService] methods.
 */
interface RoomList {
    /**
     * The loading state of the room list.
     */
    sealed interface LoadingState {
        data object NotLoaded : LoadingState
        data class Loaded(val numberOfRooms: Int) : LoadingState
    }

    /**
     * The source of the room list data.
     * All: all rooms.
     *
     * To apply some dynamic filtering on top of that, use [DynamicRoomList].
     */
    enum class Source {
        All
    }

    /**
     * The list of room summaries as a flow.
     */
    val summaries: SharedFlow<List<RoomSummary>>

    /**
     * The loading state of the room list as a flow.
     * This is useful to know if a specific set of rooms is loaded or not.
     */
    val loadingState: StateFlow<LoadingState>

    /**
     * Force a refresh of the room summaries.
     * Might be useful for some situations where we are not notified of changes.
     */
    suspend fun rebuildSummaries()
}

suspend fun RoomList.awaitLoaded(timeout: Duration = Duration.INFINITE) {
    try {
        Timber.d("awaitAllRoomsAreLoaded: wait")
        withTimeout(timeout) {
            loadingState.firstOrNull {
                it is RoomList.LoadingState.Loaded
            }
        }
    } catch (timeoutException: TimeoutCancellationException) {
        Timber.d("awaitAllRoomsAreLoaded: no response after $timeout")
    }
}
