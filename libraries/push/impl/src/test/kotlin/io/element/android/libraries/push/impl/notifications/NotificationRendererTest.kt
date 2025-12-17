/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoader
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDataFactory
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val MY_USER_DISPLAY_NAME = "display-name"
private const val MY_USER_AVATAR_URL = "avatar-url"
private const val USE_COMPLETE_NOTIFICATION_FORMAT = true

private val A_SUMMARY_NOTIFICATION = SummaryNotification.Update(A_NOTIFICATION)
private val ONE_SHOT_NOTIFICATION =
    OneShotNotification(notification = A_NOTIFICATION, tag = "ignored", summaryLine = "ignored", isNoisy = false, timestamp = -1)

@RunWith(RobolectricTestRunner::class)
class NotificationRendererTest {
    private val notificationDisplayer = FakeNotificationDisplayer()

    private val notificationCreator = FakeNotificationCreator()
    private val roomGroupMessageCreator = FakeRoomGroupMessageCreator()
    private val summaryGroupMessageCreator = FakeSummaryGroupMessageCreator()
    private val notificationDataFactory = DefaultNotificationDataFactory(
        notificationCreator = notificationCreator,
        roomGroupMessageCreator = roomGroupMessageCreator,
        summaryGroupMessageCreator = summaryGroupMessageCreator,
        activeNotificationsProvider = FakeActiveNotificationsProvider(),
        stringProvider = FakeStringProvider(),
    )
    private val notificationIdProvider = NotificationIdProvider

    private val notificationRenderer = createNotificationRenderer(
        notificationDisplayer = notificationDisplayer,
        notificationDataFactory = notificationDataFactory,
    )

    @Test
    fun `given no notifications when rendering then cancels summary notification`() = runTest {
        renderEventsAsNotifications(emptyList())

        notificationDisplayer.verifySummaryCancelled()
    }

    @Test
    fun `given a room message group notification is added when rendering then show the message notification and update summary`() = runTest {
        roomGroupMessageCreator.createRoomMessageResult = lambdaRecorder { _, _, _, _, _, _ -> A_NOTIFICATION }

        renderEventsAsNotifications(listOf(aNotifiableMessageEvent()))

        notificationDisplayer.showNotificationResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(A_ROOM_ID.value), value(notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    @Test
    fun `given a simple notification is added when rendering then show the simple notification and update summary`() = runTest {
        notificationCreator.createSimpleNotificationResult = lambdaRecorder { _, _ -> ONE_SHOT_NOTIFICATION.copy(tag = AN_EVENT_ID.value).notification }

        renderEventsAsNotifications(listOf(aSimpleNotifiableEvent(eventId = AN_EVENT_ID)))

        notificationDisplayer.showNotificationResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(AN_EVENT_ID.value), value(notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    @Test
    fun `given an invitation notification is added when rendering then show the invitation notification and update summary`() = runTest {
        notificationCreator.createRoomInvitationNotificationResult = lambdaRecorder { _, _ -> ONE_SHOT_NOTIFICATION.copy(tag = AN_EVENT_ID.value).notification }

        renderEventsAsNotifications(listOf(anInviteNotifiableEvent()))

        notificationDisplayer.showNotificationResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(A_ROOM_ID.value), value(notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    private suspend fun renderEventsAsNotifications(events: List<NotifiableEvent>) {
        notificationRenderer.render(
            MatrixUser(A_SESSION_ID, MY_USER_DISPLAY_NAME, MY_USER_AVATAR_URL),
            useCompleteNotificationFormat = USE_COMPLETE_NOTIFICATION_FORMAT,
            eventsToProcess = events,
            imageLoader = FakeImageLoader(),
        )
    }
}

fun createNotificationRenderer(
    notificationDisplayer: NotificationDisplayer = FakeNotificationDisplayer(),
    notificationDataFactory: NotificationDataFactory = FakeNotificationDataFactory(),
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    sessionStore: SessionStore = InMemorySessionStore(),
) = NotificationRenderer(
    notificationDisplayer = notificationDisplayer,
    notificationDataFactory = notificationDataFactory,
    enterpriseService = enterpriseService,
    sessionStore = sessionStore,
)
