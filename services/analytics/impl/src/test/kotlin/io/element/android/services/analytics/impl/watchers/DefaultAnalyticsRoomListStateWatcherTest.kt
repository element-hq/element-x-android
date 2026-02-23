/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.watchers

import com.google.common.truth.Truth.assertThat
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.CatchUp
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAnalyticsRoomListStateWatcherTest {
    @Test
    fun `Opening the app in a warm state tracks the time until the room list is synced`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appForegroundStateService = appForegroundStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        appForegroundStateService.givenIsInForeground(false)
        runCurrent()
        appForegroundStateService.givenIsInForeground(true)
        runCurrent()

        // The transaction should be present now
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNotNull()

        // And now the room list service running
        roomListService.postState(RoomListService.State.Running)
        runCurrent()

        // And the transaction should now be gone
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNull()

        watcher.stop()
    }

    @Test
    fun `Opening the app in a cold state does nothing`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService(
            initialForegroundValue = false
        )
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appForegroundStateService = appForegroundStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // The room list service running
        roomListService.postState(RoomListService.State.Running)
        runCurrent()

        // The transaction was never present
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNull()

        watcher.stop()
    }

    @Test
    fun `The transaction won't be finished until the room list is synchronised`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appForegroundStateService = appForegroundStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        appForegroundStateService.givenIsInForeground(false)
        runCurrent()
        appForegroundStateService.givenIsInForeground(true)
        runCurrent()

        // The transaction should be present now
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNotNull()

        runCurrent()

        // But without the room list syncing, it never finishes
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNotNull()

        watcher.stop()
    }

    @Test
    fun `Opening the app when the room list state was already Running does nothing`() = runTest {
        val appForegroundStateService = FakeAppForegroundStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Running)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appForegroundStateService = appForegroundStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        appForegroundStateService.givenIsInForeground(false)
        runCurrent()
        appForegroundStateService.givenIsInForeground(true)
        runCurrent()

        // The transaction was never added
        assertThat(analyticsService.getLongRunningTransaction(CatchUp)).isNull()

        watcher.stop()
    }

    private fun TestScope.createAnalyticsRoomListStateWatcher(
        appForegroundStateService: FakeAppForegroundStateService = FakeAppForegroundStateService(),
        roomListService: FakeRoomListService = FakeRoomListService(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
    ) = DefaultAnalyticsRoomListStateWatcher(
        appForegroundStateService = appForegroundStateService,
        roomListService = roomListService,
        analyticsService = analyticsService,
        sessionCoroutineScope = backgroundScope,
        dispatchers = testCoroutineDispatchers(),
        networkMonitor = networkMonitor,
    )
}
