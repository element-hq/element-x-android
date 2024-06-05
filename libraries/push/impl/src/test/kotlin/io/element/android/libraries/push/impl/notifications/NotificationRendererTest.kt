/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.test.notifications.FakeImageLoader
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
    OneShotNotification(notification = A_NOTIFICATION, key = "ignored", summaryLine = "ignored", isNoisy = false, timestamp = -1)

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

    private val notificationRenderer = NotificationRenderer(
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
        roomGroupMessageCreator.createRoomMessageResult = lambdaRecorder { _, _, _, _, _ -> A_NOTIFICATION }

        renderEventsAsNotifications(listOf(aNotifiableMessageEvent()))

        notificationDisplayer.showNotificationMessageResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(A_ROOM_ID.value), value(notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    @Test
    fun `given a simple notification is added when rendering then show the simple notification and update summary`() = runTest {
        notificationCreator.createSimpleNotificationResult = lambdaRecorder { _ -> ONE_SHOT_NOTIFICATION.copy(key = AN_EVENT_ID.value).notification }

        renderEventsAsNotifications(listOf(aSimpleNotifiableEvent(eventId = AN_EVENT_ID)))

        notificationDisplayer.showNotificationMessageResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(AN_EVENT_ID.value), value(notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    @Test
    fun `given an invitation notification is added when rendering then show the invitation notification and update summary`() = runTest {
        notificationCreator.createRoomInvitationNotificationResult = lambdaRecorder { _ -> ONE_SHOT_NOTIFICATION.copy(key = AN_EVENT_ID.value).notification }

        renderEventsAsNotifications(listOf(anInviteNotifiableEvent()))

        notificationDisplayer.showNotificationMessageResult.assertions().isCalledExactly(2).withSequence(
            listOf(value(A_ROOM_ID.value), value(notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID)), value(A_NOTIFICATION)),
            listOf(value(null), value(notificationIdProvider.getSummaryNotificationId(A_SESSION_ID)), value(A_SUMMARY_NOTIFICATION.notification))
        )
    }

    private suspend fun renderEventsAsNotifications(events: List<NotifiableEvent>) {
        notificationRenderer.render(
            MatrixUser(A_SESSION_ID, MY_USER_DISPLAY_NAME, MY_USER_AVATAR_URL),
            useCompleteNotificationFormat = USE_COMPLETE_NOTIFICATION_FORMAT,
            eventsToProcess = events,
            imageLoader = FakeImageLoader().getImageLoader(),
        )
    }
}
