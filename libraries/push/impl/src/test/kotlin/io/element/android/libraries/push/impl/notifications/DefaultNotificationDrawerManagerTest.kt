/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.factories.aNotificationAccountParams
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aFallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.observer.FakeSessionObserver
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.services.appnavstate.test.aNavigationState
import io.element.android.services.appnavstate.test.anAppNavigationState
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNotificationDrawerManagerTest {
    @Test
    fun `clearAllEvents should have no effect when queue is empty`() = runTest {
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager()
        defaultNotificationDrawerManager.clearAllEvents(A_SESSION_ID)
    }

    @Test
    fun `cover all APIs`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val matrixUser = aMatrixUser(id = A_SESSION_ID.value, displayName = "alice", avatarUrl = "mxc://data")
        val mockRoomGroupMessageCreator = FakeRoomGroupMessageCreator(
            createRoomMessageResult = lambdaRecorder { notificationAccountParams, _, roomId, _, _, existingNotification ->
                assertThat(notificationAccountParams.user).isEqualTo(matrixUser)
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
    }

    @Test
    fun `react to applicationStateChange`() = runTest {
        // For now just call all the API. Later, add more valuable tests.
        val appNavigationStateService = FakeAppNavigationStateService()
        createDefaultNotificationDrawerManager(
            appNavigationStateService = appNavigationStateService
        )
        appNavigationStateService.emitNavigationState(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
        appNavigationStateService.emitNavigationState(AppNavigationState(aNavigationState(A_SESSION_ID), isInForeground = true))
        runCurrent()
        appNavigationStateService.emitNavigationState(AppNavigationState(aNavigationState(A_SESSION_ID, A_ROOM_ID), isInForeground = true))
        runCurrent()
        appNavigationStateService.emitNavigationState(
            AppNavigationState(
                aNavigationState(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID),
                isInForeground = true
            )
        )
        runCurrent()
        // Like a user sign out
        appNavigationStateService.emitNavigationState(AppNavigationState(aNavigationState(), isInForeground = true))
        runCurrent()
    }

    @Test
    fun `when MatrixClient has no cached user name and avatar, the profile is loaded to render the notification`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
        val messageCreator = FakeRoomGroupMessageCreator()
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            matrixClientProvider = matrixClientProvider,
            roomGroupMessageCreator = messageCreator,
            enterpriseService = FakeEnterpriseService(
                initialBrandColor = Color.Red,
            )
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
                listOf(
                    value(aNotificationAccountParams(user = aMatrixUser(id = A_SESSION_ID.value, displayName = "alice"))),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
                listOf(
                    value(aNotificationAccountParams(user = aMatrixUser(id = A_SESSION_ID.value, displayName = ""))),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
                listOf(
                    value(aNotificationAccountParams(user = aMatrixUser(id = A_SESSION_ID.value, displayName = null, avatarUrl = null))),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            )
    }

    @Test
    fun `clearSummaryNotificationIfNeeded will run after clearing all other notifications`() = runTest {
        val cancelNotificationResult = lambdaRecorder<String?, Int, Unit> { _, _ -> }
        val notificationDisplayer = FakeNotificationDisplayer(
            cancelNotificationResult = cancelNotificationResult,
        )
        val summaryId = NotificationIdProvider.getSummaryNotificationId(A_SESSION_ID)
        val roomMessageId = NotificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID)
        val activeNotificationsProvider = FakeActiveNotificationsProvider(
            getSummaryNotificationResult = {
                mockk {
                    every { id } returns summaryId
                }
            },
            countResult = { 1 },
        )
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            notificationDisplayer = notificationDisplayer,
            activeNotificationsProvider = activeNotificationsProvider,
        )

        // Ask to clear all existing message notifications. Since only the summary notification is left, it should be cleared
        defaultNotificationDrawerManager.clearAllMessagesEvents(A_SESSION_ID)

        // Verify we asked to cancel the notification with summaryId
        cancelNotificationResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(null), value(roomMessageId)),
            listOf(value(null), value(summaryId)),
        )
    }

    @Test
    fun `when a session is signed out, clearAllEvent is invoked`() = runTest {
        val cancelNotificationResult = lambdaRecorder<String?, Int, Unit> { _, _ -> }
        val notificationDisplayer = FakeNotificationDisplayer(
            cancelNotificationResult = cancelNotificationResult,
        )
        val summaryId = NotificationIdProvider.getSummaryNotificationId(A_SESSION_ID)
        val activeNotificationsProvider = FakeActiveNotificationsProvider(
            getNotificationsForSessionResult = {
                listOf(
                    mockk {
                        every { id } returns summaryId
                        every { tag } returns null
                    },
                )
            },
            countResult = { 1 },
        )
        val sessionObserver = FakeSessionObserver()
        createDefaultNotificationDrawerManager(
            notificationDisplayer = notificationDisplayer,
            activeNotificationsProvider = activeNotificationsProvider,
            sessionObserver = sessionObserver,
        )
        // Simulate a session sign out
        sessionObserver.onSessionDeleted(A_SESSION_ID.value)
        // Verify we asked to cancel the notification with summaryId
        cancelNotificationResult.assertions().isCalledExactly(1).withSequence(
            listOf(value(null), value(summaryId)),
        )
    }

    @Test
    fun `when the application is in background, all events trigger a notification`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID),
            isInForeground = false,
        ),
        notifiableEvents = listOf(
            aFallbackNotifiableEvent(sessionId = A_SESSION_ID),
            aFallbackNotifiableEvent(sessionId = A_SESSION_ID_2),
            anInviteNotifiableEvent(sessionId = A_SESSION_ID),
            anInviteNotifiableEvent(sessionId = A_SESSION_ID_2),
            aSimpleNotifiableEvent(sessionId = A_SESSION_ID),
            aSimpleNotifiableEvent(sessionId = A_SESSION_ID_2),
            aNotifiableMessageEvent(sessionId = A_SESSION_ID),
            aNotifiableMessageEvent(sessionId = A_SESSION_ID_2),
            aNotifiableMessageEvent(sessionId = A_SESSION_ID, threadId = A_THREAD_ID),
            aNotifiableMessageEvent(sessionId = A_SESSION_ID_2, threadId = A_THREAD_ID_2),
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 2,
    )

    @Test
    fun `fallback event is ignored when the room list is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID),
        ),
        notifiableEvents = listOf(aFallbackNotifiableEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = false,
    )

    @Test
    fun `fallback event is not ignored when a room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID),
        ),
        notifiableEvents = listOf(aFallbackNotifiableEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = true,
    )

    @Test
    fun `fallback event for other session is not ignored when the room list is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID_2),
        ),
        notifiableEvents = listOf(aFallbackNotifiableEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = true,
    )

    @Test
    fun `invite notifiable event is emits a notification when the room list is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID),
        ),
        notifiableEvents = listOf(anInviteNotifiableEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `invite notifiable event does not emit a notification when the same room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID),
        ),
        notifiableEvents = listOf(
            anInviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = false,
    )

    @Test
    fun `invite notifiable event emits a notification when another room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID_2),
        ),
        notifiableEvents = listOf(
            anInviteNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `simple notifiable event is emits a notification when the room list is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID),
        ),
        notifiableEvents = listOf(aSimpleNotifiableEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `simple notifiable event does not emit a notification when the same room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID),
        ),
        notifiableEvents = listOf(
            aSimpleNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = false,
    )

    @Test
    fun `simple notifiable event emits a notification when another room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID_2),
        ),
        notifiableEvents = listOf(
            aSimpleNotifiableEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `notifiable event is emits a notification when the room list is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID),
        ),
        notifiableEvents = listOf(aNotifiableMessageEvent(sessionId = A_SESSION_ID)),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `notifiable event does not emit a notification when the same room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = false,
    )

    @Test
    fun `notifiable event for a thread emits a notification when the same room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `notifiable event for a thread does not emit a notification when the same thread is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID, threadId = A_THREAD_ID),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            )
        ),
        shouldEmitNotification = false,
    )

    @Test
    fun `notifiable event for a thread emits a notification when another thread is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID, threadId = A_THREAD_ID_2),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `notifiable event for a thread emits a notification when a thread of another room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID_2, threadId = A_THREAD_ID_2),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    @Test
    fun `notifiable event emits a notification when another room is displayed`() = testOnNotifiableEventReceived(
        appNavigationState = anAppNavigationState(
            navigationState = aNavigationState(sessionId = A_SESSION_ID, roomId = A_ROOM_ID_2),
        ),
        notifiableEvents = listOf(
            aNotifiableMessageEvent(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
            )
        ),
        shouldEmitNotification = true,
        extraInvocationsForNotificationSummary = 1,
    )

    private fun testOnNotifiableEventReceived(
        appNavigationState: AppNavigationState,
        notifiableEvents: List<NotifiableEvent>,
        shouldEmitNotification: Boolean,
        extraInvocationsForNotificationSummary: Int = 0,
    ) = runTest {
        val showNotificationResult = lambdaRecorder<String?, Int, Notification, Boolean> { _, _, _ ->
            true
        }
        val defaultNotificationDrawerManager = createDefaultNotificationDrawerManager(
            appNavigationStateService = FakeAppNavigationStateService(
                initialAppNavigationState = appNavigationState,
            ),
            notificationDisplayer = FakeNotificationDisplayer(
                showNotificationResult = showNotificationResult,
            )
        )
        defaultNotificationDrawerManager.onNotifiableEventsReceived(notifiableEvents)
        showNotificationResult.assertions().isCalledExactly(
            if (shouldEmitNotification) {
                notifiableEvents.size + extraInvocationsForNotificationSummary
            } else {
                0
            }
        )
    }
}

fun TestScope.createDefaultNotificationDrawerManager(
    notificationDisplayer: NotificationDisplayer = FakeNotificationDisplayer(),
    notificationRenderer: NotificationRenderer? = null,
    appNavigationStateService: AppNavigationStateService = FakeAppNavigationStateService(),
    roomGroupMessageCreator: RoomGroupMessageCreator = FakeRoomGroupMessageCreator(),
    summaryGroupMessageCreator: SummaryGroupMessageCreator = FakeSummaryGroupMessageCreator(),
    activeNotificationsProvider: FakeActiveNotificationsProvider = FakeActiveNotificationsProvider(),
    matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
    sessionStore: SessionStore = InMemorySessionStore(),
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    sessionObserver: SessionObserver = FakeSessionObserver(),
    analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
): DefaultNotificationDrawerManager {
    return DefaultNotificationDrawerManager(
        notificationDisplayer = notificationDisplayer,
        notificationRenderer = notificationRenderer ?: NotificationRenderer(
            notificationDisplayer = notificationDisplayer,
            notificationDataFactory = DefaultNotificationDataFactory(
                notificationCreator = FakeNotificationCreator(),
                roomGroupMessageCreator = roomGroupMessageCreator,
                summaryGroupMessageCreator = summaryGroupMessageCreator,
                activeNotificationsProvider = activeNotificationsProvider,
            ),
            enterpriseService = enterpriseService,
            sessionStore = sessionStore,
            analyticsService = analyticsService,
        ),
        appNavigationStateService = appNavigationStateService,
        coroutineScope = backgroundScope,
        matrixClientProvider = matrixClientProvider,
        imageLoaderHolder = FakeImageLoaderHolder(),
        activeNotificationsProvider = activeNotificationsProvider,
        sessionObserver = sessionObserver,
    )
}
