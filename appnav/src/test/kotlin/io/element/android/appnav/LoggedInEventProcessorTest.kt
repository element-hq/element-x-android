/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoggedInEventProcessorTest {
    @Test
    fun `test observeEvents with left membership change`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        val roomMembershipObserver = RoomMembershipObserver()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            roomMembershipObserver = roomMembershipObserver,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        testScheduler.runCurrent()

        roomMembershipObserver.notifyUserLeftRoom(
            roomId = A_ROOM_ID,
            isSpace = false,
            membershipBeforeLeft = CurrentUserMembership.JOINED,
        )

        testScheduler.runCurrent()

        // Verify that the snackbar message was displayed
        assertThat(snackbarDispatcher.snackbarMessage.first()).isNotNull()
    }

    @Test
    fun `test observeEvents with left membership change for space`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        val roomMembershipObserver = RoomMembershipObserver()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            roomMembershipObserver = roomMembershipObserver,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        testScheduler.runCurrent()

        roomMembershipObserver.notifyUserLeftRoom(
            roomId = A_ROOM_ID,
            isSpace = true,
            membershipBeforeLeft = CurrentUserMembership.JOINED,
        )

        testScheduler.runCurrent()

        // Verify that the snackbar message was displayed
        assertThat(snackbarDispatcher.snackbarMessage.first()).isNotNull()
    }

    @Test
    fun `test observeEvents with invitation rejected membership change`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        val roomMembershipObserver = RoomMembershipObserver()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            roomMembershipObserver = roomMembershipObserver,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        testScheduler.runCurrent()

        roomMembershipObserver.notifyUserLeftRoom(
            roomId = A_ROOM_ID,
            isSpace = false,
            membershipBeforeLeft = CurrentUserMembership.INVITED,
        )

        testScheduler.runCurrent()

        // Verify that the snackbar message was displayed
        assertThat(snackbarDispatcher.snackbarMessage.first()).isNotNull()
    }

    @Test
    fun `test observeEvents with knock retracted membership change`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        val roomMembershipObserver = RoomMembershipObserver()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            roomMembershipObserver = roomMembershipObserver,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        testScheduler.runCurrent()

        roomMembershipObserver.notifyUserLeftRoom(
            roomId = A_ROOM_ID,
            isSpace = false,
            membershipBeforeLeft = CurrentUserMembership.KNOCKED,
        )

        testScheduler.runCurrent()

        // Verify that the snackbar message was displayed
        assertThat(snackbarDispatcher.snackbarMessage.first()).isNotNull()
    }

    @Test
    fun `test stopObserving cancels the child scope`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        val roomMembershipObserver = RoomMembershipObserver()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            roomMembershipObserver = roomMembershipObserver,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        assertThat(loggedInEventProcessor.currentChildScope?.isActive).isTrue()

        testScheduler.runCurrent()

        // Stop observing events
        loggedInEventProcessor.stopObserving()

        // Verify that the child scope is cancelled (the scope is null)
        assertThat(loggedInEventProcessor.currentChildScope).isNull()
    }

    @Test
    fun `test notifications are cleared when room list service state is running`() = runTest {
        val clearReadNotificationsLambda = lambdaRecorder { _: SessionId -> }
        val snackbarDispatcher = SnackbarDispatcher()
        val notificationCleaner = FakeNotificationCleaner(clearReadRoomsNotificationsLambda = clearReadNotificationsLambda)
        val roomListService = FakeRoomListService()

        val loggedInEventProcessor = createLoggedInEventProcessor(
            snackbarDispatcher = snackbarDispatcher,
            notificationCleaner = notificationCleaner,
            roomListService = roomListService,
        )

        loggedInEventProcessor.observeEvents(backgroundScope)

        testScheduler.runCurrent()

        // Simulate the room list service state changing to Running
        roomListService.postState(RoomListService.State.Running)

        testScheduler.runCurrent()

        // Verify that the notifications were cleared
        clearReadNotificationsLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    private fun TestScope.createLoggedInEventProcessor(
        snackbarDispatcher: SnackbarDispatcher,
        roomMembershipObserver: RoomMembershipObserver = RoomMembershipObserver(),
        sessionId: SessionId = A_SESSION_ID,
        roomListService: FakeRoomListService = FakeRoomListService(),
        notificationCleaner: FakeNotificationCleaner = FakeNotificationCleaner(),
    ) = LoggedInEventProcessor(
        snackbarDispatcher = snackbarDispatcher,
        roomMembershipObserver = roomMembershipObserver,
        sessionId = sessionId,
        roomListService = roomListService,
        notificationCleaner = notificationCleaner,
        dispatchers = testCoroutineDispatchers(),
    )
}
