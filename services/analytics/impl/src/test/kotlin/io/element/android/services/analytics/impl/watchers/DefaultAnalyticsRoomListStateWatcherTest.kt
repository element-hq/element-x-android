/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.watchers

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.ResumeAppUntilNewRoomsReceived
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
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
        val navigationStateService = FakeAppNavigationStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appNavigationStateService = navigationStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = false))
        runCurrent()
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = true))
        runCurrent()

        // The transaction should be present now
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNotNull()

        // And now the room list service running
        roomListService.postState(RoomListService.State.Running)
        runCurrent()

        // And the transaction should now be gone
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNull()

        watcher.stop()
    }

    @Test
    fun `Opening the app in a cold state does nothing`() = runTest {
        val navigationStateService = FakeAppNavigationStateService().apply {
            appNavigationState.emit(AppNavigationState(NavigationState.Root, false))
        }
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appNavigationStateService = navigationStateService,
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
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNull()

        watcher.stop()
    }

    @Test
    fun `The transaction won't be finished until the room list is synchronised`() = runTest {
        val navigationStateService = FakeAppNavigationStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Idle)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appNavigationStateService = navigationStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = false))
        runCurrent()
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = true))
        runCurrent()

        // The transaction should be present now
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNotNull()

        runCurrent()

        // But without the room list syncing, it never finishes
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNotNull()

        watcher.stop()
    }

    @Test
    fun `Opening the app when the room list state was already Running does nothing`() = runTest {
        val navigationStateService = FakeAppNavigationStateService()
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Running)
        }
        val analyticsService = FakeAnalyticsService()
        val watcher = createAnalyticsRoomListStateWatcher(
            appNavigationStateService = navigationStateService,
            roomListService = roomListService,
            analyticsService = analyticsService,
        )

        watcher.start()

        // Give some time to load the initial state
        runCurrent()

        // Make sure it's warm by changing its internal state
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = false))
        runCurrent()
        navigationStateService.appNavigationState.emit(AppNavigationState(navigationState = NavigationState.Root, isInForeground = true))
        runCurrent()

        // The transaction was never added
        assertThat(analyticsService.getLongRunningTransaction(ResumeAppUntilNewRoomsReceived)).isNull()

        watcher.stop()
    }

    private fun TestScope.createAnalyticsRoomListStateWatcher(
        appNavigationStateService: FakeAppNavigationStateService = FakeAppNavigationStateService(),
        roomListService: FakeRoomListService = FakeRoomListService(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = DefaultAnalyticsRoomListStateWatcher(
        appNavigationStateService = appNavigationStateService,
        roomListService = roomListService,
        analyticsService = analyticsService,
        sessionCoroutineScope = backgroundScope,
        dispatchers = testCoroutineDispatchers(),
    )
}
