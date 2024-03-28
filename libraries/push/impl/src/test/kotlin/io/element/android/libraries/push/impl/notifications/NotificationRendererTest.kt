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

import android.app.Notification
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fake.FakeImageLoader
import io.element.android.libraries.push.impl.notifications.fake.MockkNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fake.MockkNotificationFactory
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val MY_USER_DISPLAY_NAME = "display-name"
private const val MY_USER_AVATAR_URL = "avatar-url"
private const val USE_COMPLETE_NOTIFICATION_FORMAT = true

private val AN_EVENT_LIST = listOf<ProcessedEvent<NotifiableEvent>>()
private val A_PROCESSED_EVENTS = GroupedNotificationEvents(emptyMap(), emptyList(), emptyList(), emptyList())
private val A_SUMMARY_NOTIFICATION = SummaryNotification.Update(mockk())
private val A_REMOVE_SUMMARY_NOTIFICATION = SummaryNotification.Removed
private val A_NOTIFICATION = mockk<Notification>()
private val MESSAGE_META = RoomNotification.Message.Meta(
    summaryLine = "ignored",
    messageCount = 1,
    latestTimestamp = -1,
    roomId = A_ROOM_ID,
    shouldBing = false
)
private val ONE_SHOT_META = OneShotNotification.Append.Meta(key = "ignored", summaryLine = "ignored", isNoisy = false, timestamp = -1)

@RunWith(RobolectricTestRunner::class)
class NotificationRendererTest {
    private val mockkNotificationDisplayer = MockkNotificationDisplayer()
    private val mockkNotificationFactory = MockkNotificationFactory()
    private val notificationIdProvider = NotificationIdProvider()

    private val notificationRenderer = NotificationRenderer(
        notificationIdProvider = notificationIdProvider,
        notificationDisplayer = mockkNotificationDisplayer.instance,
        notificationFactory = mockkNotificationFactory.instance,
    )

    @Test
    fun `given no notifications when rendering then cancels summary notification`() = runTest {
        givenNoNotifications()

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifySummaryCancelled()
        mockkNotificationDisplayer.verifyNoOtherInteractions()
    }

    @Test
    fun `given last room message group notification is removed when rendering then remove the summary and then remove message notification`() = runTest {
        givenNotifications(roomNotifications = listOf(RoomNotification.Removed(A_ROOM_ID)), summaryNotification = A_REMOVE_SUMMARY_NOTIFICATION)

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID))
            cancelNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID))
        }
    }

    @Test
    fun `given a room message group notification is removed when rendering then remove the message notification and update summary`() = runTest {
        givenNotifications(roomNotifications = listOf(RoomNotification.Removed(A_ROOM_ID)))

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID))
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    @Test
    fun `given a room message group notification is added when rendering then show the message notification and update summary`() = runTest {
        givenNotifications(
            roomNotifications = listOf(
                RoomNotification.Message(
                    A_NOTIFICATION,
                    MESSAGE_META
                )
            )
        )

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            showNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), A_NOTIFICATION)
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    @Test
    fun `given last simple notification is removed when rendering then remove the summary and then remove simple notification`() = runTest {
        givenNotifications(simpleNotifications = listOf(OneShotNotification.Removed(AN_EVENT_ID.value)), summaryNotification = A_REMOVE_SUMMARY_NOTIFICATION)

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID))
            cancelNotificationMessage(tag = AN_EVENT_ID.value, notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID))
        }
    }

    @Test
    fun `given a simple notification is removed when rendering then remove the simple notification and update summary`() = runTest {
        givenNotifications(simpleNotifications = listOf(OneShotNotification.Removed(AN_EVENT_ID.value)))

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = AN_EVENT_ID.value, notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID))
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    @Test
    fun `given a simple notification is added when rendering then show the simple notification and update summary`() = runTest {
        givenNotifications(
            simpleNotifications = listOf(
                OneShotNotification.Append(
                    A_NOTIFICATION,
                    ONE_SHOT_META.copy(key = AN_EVENT_ID.value)
                )
            )
        )

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            showNotificationMessage(tag = AN_EVENT_ID.value, notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID), A_NOTIFICATION)
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    @Test
    fun `given last invitation notification is removed when rendering then remove the summary and then remove invitation notification`() = runTest {
        givenNotifications(invitationNotifications = listOf(OneShotNotification.Removed(A_ROOM_ID.value)), summaryNotification = A_REMOVE_SUMMARY_NOTIFICATION)

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID))
            cancelNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID))
        }
    }

    @Test
    fun `given an invitation notification is removed when rendering then remove the invitation notification and update summary`() = runTest {
        givenNotifications(invitationNotifications = listOf(OneShotNotification.Removed(A_ROOM_ID.value)))

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            cancelNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID))
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    @Test
    fun `given an invitation notification is added when rendering then show the invitation notification and update summary`() = runTest {
        givenNotifications(
            simpleNotifications = listOf(
                OneShotNotification.Append(
                    A_NOTIFICATION,
                    ONE_SHOT_META.copy(key = A_ROOM_ID.value)
                )
            )
        )

        renderEventsAsNotifications()

        mockkNotificationDisplayer.verifyInOrder {
            showNotificationMessage(tag = A_ROOM_ID.value, notificationIdProvider.getRoomEventNotificationId(A_SESSION_ID), A_NOTIFICATION)
            showNotificationMessage(tag = null, notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), A_SUMMARY_NOTIFICATION.notification)
        }
    }

    private suspend fun renderEventsAsNotifications() {
        notificationRenderer.render(
            MatrixUser(A_SESSION_ID, MY_USER_DISPLAY_NAME, MY_USER_AVATAR_URL),
            useCompleteNotificationFormat = USE_COMPLETE_NOTIFICATION_FORMAT,
            eventsToProcess = AN_EVENT_LIST,
            imageLoader = FakeImageLoader().getImageLoader(),
        )
    }

    private fun givenNoNotifications() {
        givenNotifications(emptyList(), emptyList(), emptyList(), emptyList(), USE_COMPLETE_NOTIFICATION_FORMAT, A_REMOVE_SUMMARY_NOTIFICATION)
    }

    private fun givenNotifications(
        roomNotifications: List<RoomNotification> = emptyList(),
        invitationNotifications: List<OneShotNotification> = emptyList(),
        simpleNotifications: List<OneShotNotification> = emptyList(),
        fallbackNotifications: List<OneShotNotification> = emptyList(),
        useCompleteNotificationFormat: Boolean = USE_COMPLETE_NOTIFICATION_FORMAT,
        summaryNotification: SummaryNotification = A_SUMMARY_NOTIFICATION
    ) {
        mockkNotificationFactory.givenNotificationsFor(
            groupedEvents = A_PROCESSED_EVENTS,
            matrixUser = MatrixUser(A_SESSION_ID, MY_USER_DISPLAY_NAME, MY_USER_AVATAR_URL),
            useCompleteNotificationFormat = useCompleteNotificationFormat,
            roomNotifications = roomNotifications,
            invitationNotifications = invitationNotifications,
            simpleNotifications = simpleNotifications,
            fallbackNotifications = fallbackNotifications,
            summaryNotification = summaryNotification
        )
    }
}
