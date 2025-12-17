/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Optional

class FakeSpaceRoomList(
    override val roomId: RoomId = A_ROOM_ID,
    initialSpaceFlowValue: SpaceRoom? = null,
    initialSpaceRoomsValue: List<SpaceRoom> = emptyList(),
    initialSpaceRoomList: SpaceRoomList.PaginationStatus = SpaceRoomList.PaginationStatus.Loading,
    private val paginateResult: () -> Result<Unit> = { lambdaError() },
) : SpaceRoomList {
    private val currentSpaceMutableStateFlow: MutableStateFlow<Optional<SpaceRoom>> = MutableStateFlow(Optional.ofNullable(initialSpaceFlowValue))
    override val currentSpaceFlow: StateFlow<Optional<SpaceRoom>> = currentSpaceMutableStateFlow.asStateFlow()

    fun emitCurrentSpace(value: SpaceRoom?) {
        currentSpaceMutableStateFlow.value = Optional.ofNullable(value)
    }

    private val _spaceRoomsFlow: MutableStateFlow<List<SpaceRoom>> = MutableStateFlow(initialSpaceRoomsValue)
    override val spaceRoomsFlow: Flow<List<SpaceRoom>> = _spaceRoomsFlow.asStateFlow()

    fun emitSpaceRooms(value: List<SpaceRoom>) {
        _spaceRoomsFlow.value = value
    }

    private val _paginationStatusFlow: MutableStateFlow<SpaceRoomList.PaginationStatus> = MutableStateFlow(initialSpaceRoomList)
    override val paginationStatusFlow: StateFlow<SpaceRoomList.PaginationStatus> = _paginationStatusFlow.asStateFlow()

    fun emitPaginationStatus(value: SpaceRoomList.PaginationStatus) {
        _paginationStatusFlow.value = value
    }

    override suspend fun paginate(): Result<Unit> = simulateLongTask {
        paginateResult()
    }

    override fun destroy() {
        // No op
    }
}
