/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import androidx.core.app.NotificationManagerCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.services.appnavstate.test.aNavigationState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultNotificationDrawerManagerTest {
    @Test
    fun `clearAllEvents should have no effect when queue is empty`() = runTest {
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager()
        defaultNotificationDrawerManager.clearAllEvents(A_SESSION_ID)
        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `cover all APIs`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val matrixUser = aMatrixUser(id = A_SESSION_ID.value, displayName = "alice", avatarUrl = "mxc://data")
        val mockRoomGroupMessageCreator = FakeRoomGroupMessageCreator(
            createRoomMessageResult = lambdaRecorder { user, _, roomId, _, existingNotification ->
                assertThat(user).isEqualTo(matrixUser)
                assertThat(roomId).isEqualTo(A_ROOM_ID)
                assertThat(existingNotification).isNull()
                Notification()
            }
        )
        val summaryGroupMessageCreator = FakeSummaryGroupMessageCreator()
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            roomGroupMessageCreator = mockRoomGroupMessageCreator,
            summaryGroupMessageCreator = summaryGroupMessageCreator,
        )
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID)
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID)
        defaultNotificationDrawerManager.clearEvent(A_SESSION_ID, AN_EVENT_ID)
        defaultNotificationDrawerManager.clearEvent(A_SESSION_ID, AN_EVENT_ID)
        defaultNotificationDrawerManager.clearMessagesForRoom(A_SESSION_ID, A_ROOM_ID)
        defaultNotificationDrawerManager.clearMessagesForRoom(A_SESSION_ID, A_ROOM_ID)
        defaultNotificationDrawerManager.clearMembershipNotificationForSession(A_SESSION_ID)
        defaultNotificationDrawerManager.clearMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID)
        defaultNotificationDrawerManager.clearMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID)
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())
        // Add the same Event again (will be ignored)
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())
        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `react to applicationStateChange`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val appNavigationStateFlow: MutableStateFlow<AppNavigationState> = MutableStateFlow(
            AppNavigationState(
                navigationState = NavigationState.Root,
                isInForeground = true,
            )
        )
        val appNavigationStateService = FakeAppNavigationStateService(appNavigationState = appNavigationStateFlow)
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            appNavigationStateService = appNavigationStateService
        )
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID), isInForeground = true))
        runCurrent()
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID, A_THREAD_ID), isInForeground = true))
        runCurrent()
        // Like a user sign out
        appNavigationStateFlow.emit(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `when MatrixClient has no cached user name a fallback one is used to render the notification`() = runTest {
        val matrixClient = FakeMatrixClient(userDisplayName = null)
        val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        val messageCreator = FakeRoomGroupMessageCreator()
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            matrixClientProvider = matrixClientProvider,
            roomGroupMessageCreator = messageCreator,
        )
        // Gets a display name from MatrixClient.getUserProfile
        matrixClient.givenGetProfileResult(A_SESSION_ID, Result.success(aMatrixUser(id = A_SESSION_ID.value, displayName = "alice")))
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())

        // Uses the user id as a fallback value since display name is blank
        matrixClient.givenGetProfileResult(A_SESSION_ID, Result.success(aMatrixUser(id = A_SESSION_ID.value, displayName = "")))
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())

        // Uses the user id as a fallback value since the result fails
        matrixClient.givenGetProfileResult(A_SESSION_ID, Result.failure(IllegalStateException("Failed to get profile")))
        defaultNotificationDrawerManager.onNotifiableEventReceived(aNotifiableMessageEvent())

        messageCreator.createRoomMessageResult.assertions()
            .isCalledExactly(3)
            .withSequence(
                listOf(value(aMatrixUser(id = A_SESSION_ID.value, displayName = "alice")), any(), any(), any(), any()),
                listOf(value(aMatrixUser(id = A_SESSION_ID.value, displayName = A_SESSION_ID.value)), any(), any(), any(), any()),
                listOf(value(aMatrixUser(id = A_SESSION_ID.value, displayName = A_SESSION_ID.value, avatarUrl = AN_AVATAR_URL)), any(), any(), any(), any()),
            )

        defaultNotificationDrawerManager.destroy()
    }

    @Test
    fun `clearSummaryNotificationIfNeeded will run after clearing all other notifications`() = runTest {
        val notificationManager = mockk<NotificationManagerCompat> {
            every { cancel(any(), any()) } returns Unit
        }
        val summaryId = NotificationIdProvider.getSummaryNotificationId(A_SESSION_ID)
        val activeNotificationsProvider = FakeActiveNotificationsProvider(
            getSummaryNotificationResult = {
                mockk {
                    every { id } returns summaryId
                }
            },
            countResult = { 1 },
        )
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            notificationManager = notificationManager,
            activeNotificationsProvider = activeNotificationsProvider,
        )

        // Ask to clear all existing message notifications. Since only the summary notification is left, it should be cleared
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID)

        // Verify we asked to cancel the notification with summaryId
        verify { notificationManager.cancel(null, summaryId) }

        defaultNotificationDrawerManager.destroy()
    }

    private fun TestScope.createDefaultNotificationDrawerManager(
        notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(RuntimeEnvironment.getApplication()),
        appNavigationStateService: AppNavigationStateService = FakeAppNavigationStateService(),
        roomGroupMessageCreator: RoomGroupMessageCreator = FakeRoomGroupMessageCreator(),
        summaryGroupMessageCreator: SummaryGroupMessageCreator = FakeSummaryGroupMessageCreator(),
        activeNotificationsProvider: FakeActiveNotificationsProvider = FakeActiveNotificationsProvider(),
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
    ): DefaultNotificationDrawerManager {
        val context = RuntimeEnvironment.getApplication()
        return DefaultNotificationDrawerManager(
            notificationManager = notificationManager,
            notificationRenderer = NotificationRenderer(
                notificationDisplayer = DefaultNotificationDisplayer(context, NotificationManagerCompat.from(context)),
                notificationDataFactory = DefaultNotificationDataFactory(
                    notificationCreator = FakeNotificationCreator(),
                    roomGroupMessageCreator = roomGroupMessageCreator,
                    summaryGroupMessageCreator = summaryGroupMessageCreator,
                    activeNotificationsProvider = activeNotificationsProvider,
                    stringProvider = FakeStringProvider(),
                ),
            ),
            appNavigationStateService = appNavigationStateService,
            coroutineScope = this,
            matrixClientProvider = matrixClientProvider,
            imageLoaderHolder = FakeImageLoaderHolder(),
            activeNotificationsProvider = activeNotificationsProvider,
        )
    }
}
