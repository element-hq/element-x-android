/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import org.matrix.rustcomponents.sdk.SpaceRoom
import org.matrix.rustcomponents.sdk.SpaceRoomList
import org.matrix.rustcomponents.sdk.SpaceRoomListEntriesListener
import org.matrix.rustcomponents.sdk.SpaceRoomListPaginationStateListener
import org.matrix.rustcomponents.sdk.TaskHandle
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState

class FakeFfiSpaceRoomList(
    private val paginateResult: () -> Unit = { lambdaError() },
    private val paginationStateResult: () -> SpaceRoomListPaginationState = { lambdaError() },
    private val roomsResult: () -> List<SpaceRoom> = { lambdaError() },
) : SpaceRoomList(NoHandle) {
    private var spaceRoomListPaginationStateListener: SpaceRoomListPaginationStateListener? = null
    private var spaceRoomListEntriesListener: SpaceRoomListEntriesListener? = null

    override suspend fun paginate() = simulateLongTask {
        paginateResult()
    }

    override fun paginationState(): SpaceRoomListPaginationState {
        return paginationStateResult()
    }

    override fun rooms(): List<SpaceRoom> {
        return roomsResult()
    }

    override fun subscribeToPaginationStateUpdates(listener: SpaceRoomListPaginationStateListener): TaskHandle {
        spaceRoomListPaginationStateListener = listener
        return FakeFfiTaskHandle()
    }

    fun triggerPaginationStateUpdate(state: SpaceRoomListPaginationState) {
        spaceRoomListPaginationStateListener?.onUpdate(state)
    }

    override fun subscribeToRoomUpdate(listener: SpaceRoomListEntriesListener): TaskHandle {
        spaceRoomListEntriesListener = listener
        return FakeFfiTaskHandle()
    }

    fun triggerRoomListUpdate(rooms: List<SpaceListUpdate>) {
        spaceRoomListEntriesListener?.onUpdate(rooms)
    }
}
