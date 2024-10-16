/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.datasource

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.impl.FakeDateTimeObserver
import io.element.android.libraries.androidutils.system.DateTimeObserver
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class RoomListDataSourceTest {
    @Test
    fun `when DateTimeObserver gets a date change, the room summaries are refreshed`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Running)
            postAllRooms(listOf(aRoomSummary()))
        }
        val dateTimeObserver = FakeDateTimeObserver()
        val roomListDataSource = createRoomListDataSource(roomListService = roomListService, dateTimeObserver = dateTimeObserver)

        roomListDataSource.allRooms.test {
            // Observe room list items changes
            roomListDataSource.launchIn(backgroundScope)

            // Get the initial room list
            val initialRoomList = awaitItem()
            assertThat(initialRoomList).isNotEmpty()

            // Trigger a date change
            dateTimeObserver.given(DateTimeObserver.Event.DateChanged(Instant.MIN, Instant.now()))

            // Check there is a new list and it's not the same as the previous one (although it has the same content)
            val newRoomList = awaitItem()
            assertThat(newRoomList).isNotSameInstanceAs(initialRoomList)
        }
    }

    @Test
    fun `when DateTimeObserver gets a time zone change, the room summaries are refreshed`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postState(RoomListService.State.Running)
            postAllRooms(listOf(aRoomSummary()))
        }
        val dateTimeObserver = FakeDateTimeObserver()
        val roomListDataSource = createRoomListDataSource(roomListService = roomListService, dateTimeObserver = dateTimeObserver)

        roomListDataSource.allRooms.test {
            // Observe room list items changes
            roomListDataSource.launchIn(backgroundScope)

            // Get the initial room list
            val initialRoomList = awaitItem()
            assertThat(initialRoomList).isNotEmpty()

            // Trigger a timezone change
            dateTimeObserver.given(DateTimeObserver.Event.TimeZoneChanged)

            // Check there is a new list and it's not the same as the previous one (although it has the same content)
            val newRoomList = awaitItem()
            assertThat(newRoomList).isNotSameInstanceAs(initialRoomList)
        }
    }

    private fun TestScope.createRoomListDataSource(
        roomListService: FakeRoomListService = FakeRoomListService(),
        roomListRoomSummaryFactory: RoomListRoomSummaryFactory = RoomListRoomSummaryFactory(
            lastMessageTimestampFormatter = { _ -> "Yesterday" },
            roomLastMessageFormatter = { _, _ -> "Hey" }
        ),
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        dateTimeObserver: FakeDateTimeObserver = FakeDateTimeObserver(),
    ) = RoomListDataSource(
        roomListService = roomListService,
        roomListRoomSummaryFactory = roomListRoomSummaryFactory,
        coroutineDispatchers = testCoroutineDispatchers(),
        notificationSettingsService = notificationSettingsService,
        appScope = backgroundScope,
        dateTimeObserver = dateTimeObserver,
    )
}
