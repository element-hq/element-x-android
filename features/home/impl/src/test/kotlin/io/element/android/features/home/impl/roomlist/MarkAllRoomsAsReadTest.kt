/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkAllRoomsAsReadTest {
    @Test
    fun `hasUnreadRooms - returns true when unread list has rooms`() = runTest {
        val unreadList = FakeDynamicRoomList(
            loadingState = MutableStateFlow(RoomList.LoadingState.Loaded(numberOfRooms = 2)),
        )
        val markAllRoomsAsRead = createMarkAllRoomsAsRead(
            unreadList = unreadList,
        )

        assertThat(markAllRoomsAsRead.hasUnreadRooms()).isTrue()
        assertThat(unreadList.currentFilter.value).isEqualTo(RoomListFilter.Unread)
    }

    @Test
    fun `hasUnreadRooms - returns false when unread list is empty`() = runTest {
        val unreadList = FakeDynamicRoomList(
            loadingState = MutableStateFlow(RoomList.LoadingState.Loaded(numberOfRooms = 0)),
        )
        val markAllRoomsAsRead = createMarkAllRoomsAsRead(
            unreadList = unreadList,
        )

        assertThat(markAllRoomsAsRead.hasUnreadRooms()).isFalse()
    }

    @Test
    fun `invoke - marks all unread rooms and clears all notifications`() = runTest {
        val room1 = aRoomSummary(roomId = A_ROOM_ID)
        val room2 = aRoomSummary(roomId = A_ROOM_ID_2)
        val room3 = aRoomSummary(roomId = A_ROOM_ID_3)
        val summariesFlow = MutableStateFlow(listOf(room1))
        val loadingStateFlow = MutableStateFlow<RoomList.LoadingState>(RoomList.LoadingState.Loaded(numberOfRooms = 3))
        var loadMoreCount = 0
        val unreadList = FakeDynamicRoomList(
            summaries = summariesFlow,
            loadingState = loadingStateFlow,
            pageSize = 1,
            loadMoreLambda = {
                loadMoreCount++
                summariesFlow.value = when (loadMoreCount) {
                    1 -> listOf(room1, room2)
                    else -> listOf(room1, room2, room3)
                }
            },
        )
        val markRoomAsRead = FakeMarkRoomAsRead()
        val clearAllMessagesEventsLambda = lambdaRecorder<io.element.android.libraries.matrix.api.core.SessionId, Unit> { }
        val markAllRoomsAsRead = createMarkAllRoomsAsRead(
            unreadList = unreadList,
            markRoomAsRead = markRoomAsRead,
            clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.processedCount).isEqualTo(3)
        assertThat(result.getOrNull()?.failedCount).isEqualTo(0)
        assertThat(markRoomAsRead.invokedRoomIds).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
        assertThat(loadMoreCount).isEqualTo(2)
        clearAllMessagesEventsLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `invoke - continues when marking a room fails`() = runTest {
        val room1 = aRoomSummary(roomId = A_ROOM_ID)
        val room2 = aRoomSummary(roomId = A_ROOM_ID_2)
        val unreadList = FakeDynamicRoomList(
            summaries = MutableStateFlow(listOf(room1, room2)),
            loadingState = MutableStateFlow(RoomList.LoadingState.Loaded(numberOfRooms = 2)),
        )
        val markRoomAsRead = FakeMarkRoomAsRead { roomId ->
            if (roomId == A_ROOM_ID) {
                Result.failure(IllegalStateException("Failed"))
            } else {
                Result.success(Unit)
            }
        }
        val markAllRoomsAsRead = createMarkAllRoomsAsRead(
            unreadList = unreadList,
            markRoomAsRead = markRoomAsRead,
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.processedCount).isEqualTo(1)
        assertThat(result.getOrNull()?.failedCount).isEqualTo(1)
        assertThat(markRoomAsRead.invokedRoomIds).containsExactly(A_ROOM_ID, A_ROOM_ID_2)
    }

    private fun TestScope.createMarkAllRoomsAsRead(
        unreadList: FakeDynamicRoomList,
        markRoomAsRead: MarkRoomAsRead = FakeMarkRoomAsRead(),
        clearAllMessagesEventsLambda: (io.element.android.libraries.matrix.api.core.SessionId) -> Unit = {},
    ): DefaultMarkAllRoomsAsRead {
        val roomListService = FakeRoomListService(
            createRoomListLambda = { unreadList },
        )
        return DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(roomListService = roomListService),
            roomListService = roomListService,
            markRoomAsRead = markRoomAsRead,
            notificationCleaner = FakeNotificationCleaner(clearAllMessagesEventsLambda = clearAllMessagesEventsLambda),
            coroutineDispatchers = testCoroutineDispatchers(),
            sessionCoroutineScope = backgroundScope,
        )
    }
}
