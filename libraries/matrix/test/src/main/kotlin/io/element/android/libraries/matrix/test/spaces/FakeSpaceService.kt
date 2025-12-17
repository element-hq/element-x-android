/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceHandle
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeSpaceService(
    private val joinedSpacesResult: () -> Result<List<SpaceRoom>> = { lambdaError() },
    private val spaceRoomListResult: (RoomId) -> SpaceRoomList = { lambdaError() },
    private val leaveSpaceHandleResult: (RoomId) -> LeaveSpaceHandle = { lambdaError() },
) : SpaceService {
    private val _spaceRoomsFlow = MutableSharedFlow<List<SpaceRoom>>()
    override val spaceRoomsFlow: SharedFlow<List<SpaceRoom>>
        get() = _spaceRoomsFlow.asSharedFlow()

    suspend fun emitSpaceRoomList(value: List<SpaceRoom>) {
        _spaceRoomsFlow.emit(value)
    }

    override suspend fun joinedSpaces(): Result<List<SpaceRoom>> = simulateLongTask {
        return joinedSpacesResult()
    }

    override fun spaceRoomList(id: RoomId): SpaceRoomList {
        return spaceRoomListResult(id)
    }

    override fun getLeaveSpaceHandle(spaceId: RoomId): LeaveSpaceHandle {
        return leaveSpaceHandleResult(spaceId)
    }
}
