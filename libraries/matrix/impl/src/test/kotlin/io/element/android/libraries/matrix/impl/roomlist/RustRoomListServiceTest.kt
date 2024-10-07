/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.libraries.matrix.impl.roomlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoomListService
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import org.matrix.rustcomponents.sdk.RoomListService as RustRoomListService

@OptIn(ExperimentalCoroutinesApi::class)
class RustRoomListServiceTest {
    @Test
    fun `syncIndicator should emit the expected values`() = runTest {
        val roomListService = FakeRustRoomListService()
        val sut = createRustRoomListService(
            sessionCoroutineScope = backgroundScope,
            roomListService = roomListService,
        )
        // Give time for mxCallback to setup
        runCurrent()
        sut.syncIndicator.test {
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Hide)
            roomListService.emitRoomListServiceSyncIndicator(RoomListServiceSyncIndicator.SHOW)
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Show)
            roomListService.emitRoomListServiceSyncIndicator(RoomListServiceSyncIndicator.HIDE)
            assertThat(awaitItem()).isEqualTo(RoomListService.SyncIndicator.Hide)
        }
    }
}

private fun TestScope.createRustRoomListService(
    sessionCoroutineScope: CoroutineScope,
    roomListService: RustRoomListService = FakeRustRoomListService(),
) = RustRoomListService(
    innerRoomListService = roomListService,
    sessionDispatcher = StandardTestDispatcher(testScheduler),
    roomListFactory = RoomListFactory(
        innerRoomListService = roomListService,
        sessionCoroutineScope = sessionCoroutineScope,
    ),
    roomSyncSubscriber = RoomSyncSubscriber(
        roomListService = roomListService,
        dispatchers = testCoroutineDispatchers(),
    ),
    sessionCoroutineScope = sessionCoroutineScope,
)
