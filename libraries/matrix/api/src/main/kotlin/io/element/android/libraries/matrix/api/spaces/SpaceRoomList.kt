/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.Optional

interface SpaceRoomList {
    sealed interface PaginationStatus {
        data object Loading : PaginationStatus
        data class Idle(val hasMoreToLoad: Boolean) : PaginationStatus
    }

    val roomId: RoomId

    val currentSpaceFlow: StateFlow<Optional<SpaceRoom>>

    val spaceRoomsFlow: Flow<List<SpaceRoom>>
    val paginationStatusFlow: StateFlow<PaginationStatus>
    suspend fun paginate(): Result<Unit>

    fun destroy()
}
