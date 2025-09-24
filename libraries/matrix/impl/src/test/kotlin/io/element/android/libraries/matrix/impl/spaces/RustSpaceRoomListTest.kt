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
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import kotlin.jvm.optionals.getOrNull
import org.matrix.rustcomponents.sdk.SpaceRoomList as InnerSpaceRoomList

class RustSpaceRoomListTest {
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

    @Test
    fun `currentSpaceFlow reads value from the SpaceRoomCache`() = runTest {
        val spaceRoomCache = SpaceRoomCache()
        val sut = createRustSpaceRoomList(
            spaceRoomCache = spaceRoomCache,
        )
        sut.currentSpaceFlow().test {
            assertThat(awaitItem().getOrNull()).isNull()
            val spaceRoom = aSpaceRoom(roomId = A_ROOM_ID)
            spaceRoomCache.update(listOf(spaceRoom))
            assertThat(awaitItem().getOrNull()).isEqualTo(spaceRoom)
        }
    }

    private fun TestScope.createRustSpaceRoomList(
        roomId: RoomId = A_ROOM_ID,
        innerSpaceRoomList: InnerSpaceRoomList = FakeFfiSpaceRoomList(),
        innerProvider: suspend () -> InnerSpaceRoomList = { innerSpaceRoomList },
        spaceRoomMapper: SpaceRoomMapper = SpaceRoomMapper(),
        spaceRoomCache: SpaceRoomCache = SpaceRoomCache(),
    ): RustSpaceRoomList {
        return RustSpaceRoomList(
            roomId = roomId,
            innerProvider = innerProvider,
            sessionCoroutineScope = backgroundScope,
            spaceRoomMapper = spaceRoomMapper,
            spaceRoomCache = spaceRoomCache,
        )
    }
}
