/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.matrix.impl.spaces

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSpaceRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiSpaceRoomList
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import org.matrix.rustcomponents.sdk.SpaceRoomList as InnerSpaceRoomList

class RustSpaceRoomListTest {
    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `paginationStatusFlow emits values`() = runTest {
        val innerSpaceRoomList = FakeFfiSpaceRoomList(
            paginationStateResult = { SpaceRoomListPaginationState.Idle(false) }
        )
        val sut = createRustSpaceRoomList(
            innerSpaceRoomList = innerSpaceRoomList,
        )
        sut.paginationStatusFlow.test {
            // First value is the initial one
            assertThat(awaitItem()).isEqualTo(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false))
            // First value after the subscription occurs
            assertThat(awaitItem()).isEqualTo(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = true))
            innerSpaceRoomList.triggerPaginationStateUpdate(SpaceRoomListPaginationState.Loading)
            assertThat(awaitItem()).isEqualTo(SpaceRoomList.PaginationStatus.Loading)
            innerSpaceRoomList.triggerPaginationStateUpdate(SpaceRoomListPaginationState.Idle(true))
            assertThat(awaitItem()).isEqualTo(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false))
            innerSpaceRoomList.triggerPaginationStateUpdate(SpaceRoomListPaginationState.Idle(false))
            assertThat(awaitItem()).isEqualTo(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = true))
        }
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `spaceRoomsFlow emits values`() = runTest {
        val innerSpaceRoomList = FakeFfiSpaceRoomList(
            paginationStateResult = { SpaceRoomListPaginationState.Idle(false) }
        )
        val sut = createRustSpaceRoomList(
            innerSpaceRoomList = innerSpaceRoomList,
        )
        sut.spaceRoomsFlow.test {
            // Give time for the subscription to be set
            runCurrent()
            innerSpaceRoomList.triggerRoomListUpdate(
                listOf(
                    SpaceListUpdate.PushBack(aRustSpaceRoom(roomId = A_ROOM_ID_2))
                )
            )
            val rooms = awaitItem()
            assertThat(rooms).hasSize(1)
            assertThat(rooms[0].roomId).isEqualTo(A_ROOM_ID_2)
        }
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `paginate invokes paginate on the inner class`() = runTest {
        val paginateResult = lambdaRecorder<Unit> { }
        val innerSpaceRoomList = FakeFfiSpaceRoomList(
            paginateResult = paginateResult,
        )
        val sut = createRustSpaceRoomList(
            innerSpaceRoomList = innerSpaceRoomList,
        )
        sut.paginate()
        paginateResult.assertions().isCalledOnce()
    }

    private fun TestScope.createRustSpaceRoomList(
        roomId: RoomId = A_ROOM_ID,
        innerSpaceRoomList: InnerSpaceRoomList = FakeFfiSpaceRoomList(),
        innerProvider: suspend () -> InnerSpaceRoomList = { innerSpaceRoomList },
        spaceRoomMapper: SpaceRoomMapper = SpaceRoomMapper(),
    ): RustSpaceRoomList {
        return RustSpaceRoomList(
            roomId = roomId,
            innerProvider = innerProvider,
            coroutineScope = backgroundScope,
            spaceRoomMapper = spaceRoomMapper,
        )
    }
}
