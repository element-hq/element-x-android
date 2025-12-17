/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.matrix.test.A_THREAD_ID
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
            aStatusBarNotification(id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value),
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

        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID, A_ROOM_ID, null)).hasSize(1)
        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID_2, A_ROOM_ID_2, null)).isEmpty()
    }

    @Test
    fun `getMessageNotificationsForRoom with thread id returns only message notifications for a thread using those session and room ids`() {
        val activeNotifications = listOf(
            aStatusBarNotification(
                id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID),
                groupId = A_SESSION_ID.value,
                tag = "$A_ROOM_ID|$A_THREAD_ID",
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID), groupId = A_SESSION_ID.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomMessagesNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = "$A_ROOM_ID|$A_THREAD_ID",
            ),
            aStatusBarNotification(id = notificationIdProvider.getSummaryNotificationId(A_SESSION_ID_2), groupId = A_SESSION_ID_2.value, tag = A_ROOM_ID.value),
            aStatusBarNotification(
                id = notificationIdProvider.getRoomInvitationNotificationId(A_SESSION_ID_2),
                groupId = A_SESSION_ID_2.value,
                tag = "$A_ROOM_ID|$A_THREAD_ID",
            ),
        )
        val activeNotificationsProvider = createActiveNotificationsProvider(activeNotifications = activeNotifications)

        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID, A_ROOM_ID, A_THREAD_ID)).hasSize(1)
        assertThat(activeNotificationsProvider.getMessageNotificationsForRoom(A_SESSION_ID_2, A_ROOM_ID_2, A_THREAD_ID)).isEmpty()
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
