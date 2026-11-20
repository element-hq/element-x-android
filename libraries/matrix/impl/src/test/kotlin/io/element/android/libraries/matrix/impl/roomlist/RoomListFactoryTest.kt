/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomList
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListDynamicEntriesController
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListEntriesWithDynamicAdaptersResult
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class RoomListFactoryTest {
    @Test
    fun `createRoomList should work`() = runTest {
        val sut = RoomListFactory(
            innerRoomListService = FakeFfiRoomListService(),
            analyticsService = FakeAnalyticsService(),
        )
        sut.createRoomList(
            pageSize = 10,
            coroutineContext = EmptyCoroutineContext,
            coroutineScope = backgroundScope,
        ) {
            FakeFfiRoomList()
        }
    }

    @Test
    fun `when the room list scope is cancelled, the destroyed controller is not used anymore`() = runTest {
        val controller = FakeFfiRoomListDynamicEntriesController()
        val roomListScope = CoroutineScope(coroutineContext + Job())
        val sut = RoomListFactory(
            innerRoomListService = FakeFfiRoomListService(),
            analyticsService = FakeAnalyticsService(),
        )
        val roomList = sut.createRoomList(
            pageSize = 10,
            coroutineContext = EmptyCoroutineContext,
            coroutineScope = roomListScope,
        ) {
            FakeFfiRoomList(
                entriesWithDynamicAdaptersResult = { FakeFfiRoomListEntriesWithDynamicAdaptersResult(controller) },
            )
        }
        runCurrent()
        roomListScope.cancel()
        runCurrent()
        assertThat(controller.isDestroyed).isTrue()
        roomList.loadMore()
    }
}
