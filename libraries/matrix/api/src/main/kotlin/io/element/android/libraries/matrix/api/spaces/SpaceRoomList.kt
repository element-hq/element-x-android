/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SpaceRoomList {
    sealed interface PaginationStatus {
        data object Loading : PaginationStatus
        data class Idle(val hasMoreToLoad: Boolean) : PaginationStatus
    }

    fun currentSpaceFlow(): Flow<SpaceRoom?>

    val spaceRoomsFlow: Flow<List<SpaceRoom>>
    val paginationStatusFlow: StateFlow<PaginationStatus>
    suspend fun paginate(): Result<Unit>
}

