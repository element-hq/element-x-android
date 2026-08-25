/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Optional

/**
 * The paginated list of the children of a single space, kept up to date by the SDK.
 *
 * Obtain an instance from [SpaceService.spaceRoomList]; the caller owns it and must call [destroy] when done.
 */
interface SpaceRoomList {
    sealed interface PaginationStatus {
        data object Loading : PaginationStatus
        data class Idle(val hasMoreToLoad: Boolean) : PaginationStatus
    }

    /** The space whose children this list holds. */
    val spaceId: RoomId

    /** What is known about the space itself, starting empty until the SDK provides it. */
    val currentSpaceFlow: StateFlow<Optional<SpaceRoom>>

    /** The children loaded so far, re-emitted in full on every update; the latest value is replayed to new collectors. */
    val spaceRoomsFlow: Flow<List<SpaceRoom>>

    /** Whether a page is being loaded and whether more children remain; see [loadAllIncrementally] to drain the list. */
    val paginationStatusFlow: StateFlow<PaginationStatus>

    /** Loads the next page of children, which will be reflected in [spaceRoomsFlow]. */
    suspend fun paginate(): Result<Unit>

    /** Discards the loaded children and starts the list again from the beginning. */
    suspend fun reset(): Result<Unit>

    /** Releases the SDK resources and cancels the internal scope; the list is unusable afterwards. */
    fun destroy()
}

/**
 * Loads all space rooms incrementally by automatically paginating whenever more data is available.
 * This function observes the pagination status and triggers [paginate] calls until the entire list is loaded.
 *
 * @param coroutineScope The scope in which the pagination flow will be collected.
 */
fun SpaceRoomList.loadAllIncrementally(coroutineScope: CoroutineScope) {
    paginationStatusFlow
        .onEach { paginationStatus ->
            if (paginationStatus is SpaceRoomList.PaginationStatus.Idle && paginationStatus.hasMoreToLoad) {
                paginate()
            }
        }
        .launchIn(coroutineScope)
}
