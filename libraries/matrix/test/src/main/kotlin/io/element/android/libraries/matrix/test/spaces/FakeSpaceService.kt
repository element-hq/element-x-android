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
import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeSpaceService(
    private val spaceRoomListResult: (RoomId) -> SpaceRoomList = { lambdaError() },
    private val leaveSpaceHandleResult: (RoomId) -> LeaveSpaceHandle = { lambdaError() },
    private val removeChildFromSpaceResult: (RoomId, RoomId) -> Result<Unit> = { _, _ -> lambdaError() },
    private val joinedParentsResult: (RoomId) -> Result<List<SpaceRoom>> = { lambdaError() },
    private val getSpaceRoomResult: (RoomId) -> SpaceRoom? = { lambdaError() },
    private val editableSpacesResult: () -> Result<List<SpaceRoom>> = { lambdaError() },
    private val addChildToSpaceResult: (RoomId, RoomId) -> Result<Unit> = { _, _ -> lambdaError() },
) : SpaceService {
    private val _topLevelSpacesFlow = MutableSharedFlow<List<SpaceRoom>>()
    override val topLevelSpacesFlow: SharedFlow<List<SpaceRoom>>
        get() = _topLevelSpacesFlow.asSharedFlow()

    suspend fun emitTopLevelSpaces(value: List<SpaceRoom>) {
        _topLevelSpacesFlow.emit(value)
    }

    private val _spaceFiltersFlow = MutableSharedFlow<List<SpaceServiceFilter>>()
    override val spaceFiltersFlow: SharedFlow<List<SpaceServiceFilter>>
        get() = _spaceFiltersFlow.asSharedFlow()

    suspend fun emitSpaceFilters(value: List<SpaceServiceFilter>) {
        _spaceFiltersFlow.emit(value)
    }

    override suspend fun joinedParents(spaceId: RoomId): Result<List<SpaceRoom>> {
        return joinedParentsResult(spaceId)
    }

    override suspend fun getSpaceRoom(spaceId: RoomId): SpaceRoom? {
        return getSpaceRoomResult(spaceId)
    }

    override fun spaceRoomList(id: RoomId): SpaceRoomList {
        return spaceRoomListResult(id)
    }

    override suspend fun editableSpaces(): Result<List<SpaceRoom>> {
        return editableSpacesResult()
    }

    override fun getLeaveSpaceHandle(spaceId: RoomId): LeaveSpaceHandle {
        return leaveSpaceHandleResult(spaceId)
    }

    override suspend fun addChildToSpace(spaceId: RoomId, childId: RoomId): Result<Unit> = simulateLongTask {
        addChildToSpaceResult(spaceId, childId)
    }

    override suspend fun removeChildFromSpace(spaceId: RoomId, childId: RoomId): Result<Unit> = simulateLongTask {
        removeChildFromSpaceResult(spaceId, childId)
    }
}
