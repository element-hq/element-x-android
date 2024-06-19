/*
 * Copyright (c) 2024 New Vector Ltd
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
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultActiveNotificationsProviderTest {
    private val notificationIdProvider = NotificationIdProvider

    @Test
    fun `getAllNotifications with no active notifications returns empty list`() {
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = emptyList())

        val emptyNotifications = activeNotificationsProvider.getAllNotifications()
        assertThat(emptyNotifications).isEmpty()
    }

    @Test
    fun `getAllNotifications with active notifications returns all`() {
        val activeNotifications = listOf(
            aStatusBarNotification(id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
            aStatusBarNotification(id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        val result = activeNotificationsProvider.getAllNotifications()
        assertThat(result).hasSize(3)
    }

    @Test
    fun `getNotificationsForSession returns only notifications for that session id`() {
        val activeNotifications = listOf(
            aStatusBarNotification(id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value),
            aStatusBarNotification(id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getNotificationsForSession(A_SESSION_ID)).hasSize(1)
        assertThat(activeNotificationsProvider.getNotificationsForSession(A_SESSION_ID_2)).hasSize(2)
    }

    @Test
    fun `getMembershipNotificationsForSession returns only membership notifications for that session id`() {
        val activeNotifications = listOf(
            aStatusBarNotification(id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value,),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = A_ROOM_ID.value,
            ),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getMembershipNotificationForSession(A_SESSION_ID)).isEmpty()
        assertThat(activeNotificationsProvider.getMembershipNotificationForSession(A_SESSION_ID_2)).hasSize(1)
    }

    @Test
    fun `getMessageNotificationsForRoom returns only message notifications for those session and room ids`() {
        val activeNotifications = listOf(
            aStatusBarNotification(
                id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID),
                groupId = A_SESSION_ID.value,
                tag = A_ROOM_ID.value
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = A_ROOM_ID.value
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = A_ROOM_ID.value
            ),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID, A_ROOM_ID)).hasSize(1)
        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID_2, A_ROOM_ID_2)).isEmpty()
    }

    @Test
    fun `getMembershipNotificationsForRoom returns only membership notifications for those session and room ids`() {
        val activeNotifications = listOf(
            aStatusBarNotification(
                id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID),
                groupId = A_SESSION_ID.value,
                tag = A_ROOM_ID.value
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = A_ROOM_ID_2.value
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = A_ROOM_ID_2.value
            ),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID)).isEmpty()
        assertThat(activeNotificationsProvider.getMembershipNotificationForRoom(A_SESSION_ID_2, A_ROOM_ID_2)).hasSize(2)
    }

    @Test
    fun `getSummaryNotification returns only the summary notification for that session id if it exists`() {
        val activeNotifications = listOf(
            aStatusBarNotification(id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
            aStatusBarNotification(id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getSummaryNotification(A_SESSION_ID)).isNotNull()
        assertThat(activeNotificationsProvider.getSummaryNotification(A_SESSION_ID_2)).isNull()
    }

    private fun aStatusBarNotification(id: Int, groupId: String, tag: String? = null) = mockk<StatusBarNotification> {
        every { this@mockk.id } returns id
        every { this@mockk.tag } returns tag
        @Suppress("DEPRECATION")
        every { this@mockk.notification } returns Notification.Builder(InstrumentationRegistry.getInstrumentation().targetContext).setGroup(groupId).build()
    }

    private fun createActiveNotificationsProvider(
        activeNotifications: List<StatusBarNotification> = emptyList(),
    ): DefaultActiveNotificationsProvider {
        val notificationManager = mockk<NotificationManagerCompat> {
            every { this@mockk.activeNotifications } returns activeNotifications
        }
        return DefaultActiveNotificationsProvider(
            notificationManager = notificationManager,
        )
    }
}
