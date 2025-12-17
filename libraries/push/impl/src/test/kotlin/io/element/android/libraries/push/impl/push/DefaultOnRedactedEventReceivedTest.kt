/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.push.impl.notifications.factories.DefaultNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultOnRedactedEventReceivedTest {
    private val fakePerson = Person.Builder().setName(A_USER_NAME).setKey(A_USER_ID.value).build()
    private val fakeMessage = NotificationCompat.MessagingStyle.Message("A message", 0L, fakePerson).also {
        it.extras.putString(DefaultNotificationCreator.MESSAGE_EVENT_ID, AN_EVENT_ID.value)
    }
    private val fakeNotification = NotificationCompat.Builder(InstrumentationRegistry.getInstrumentation().targetContext, "aChannel")
        .setStyle(
            NotificationCompat.MessagingStyle(fakePerson)
                .addMessage(fakeMessage)
        )
        .setGroup(A_SESSION_ID.value)
        .build()

    private val fakeIncorrectMessage = NotificationCompat.MessagingStyle.Message("The wrong message", 0L, fakePerson).also {
        it.extras.putString(DefaultNotificationCreator.MESSAGE_EVENT_ID, AN_EVENT_ID_2.value)
    }
    private val fakeIncorrectNotification = NotificationCompat.Builder(InstrumentationRegistry.getInstrumentation().targetContext, "aChannel")
        .setGroup(A_SESSION_ID.value)
        .setStyle(
            NotificationCompat.MessagingStyle(fakePerson)
                .addMessage(fakeIncorrectMessage)
        )
        .build()

    @Test
    fun `when no notifications are found, nothing happen`() = runTest {
        val showNotificationLambda = lambdaRecorder<String?, Int, Notification, Boolean> { _, _, _ -> true }
        val sut = createDefaultOnRedactedEventReceived(
            getAllMessageNotificationsForRoomResult = { _, _ -> emptyList() },
            displayer = FakeNotificationDisplayer(showNotificationLambda),
        )
        sut.onRedactedEventsReceived(listOf(ResolvedPushEvent.Redaction(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, null)))
        showNotificationLambda.assertions().isNeverCalled()
    }

    @Test
    fun `when a notification is found, try to retrieve the message`() = runTest {
        val showNotificationLambda = lambdaRecorder<String?, Int, Notification, Boolean> { tag, id, _ ->
            assertThat(tag).isEqualTo(A_ROOM_ID.value)
            assertThat(id).isEqualTo(1)
            true
        }
        val sut = createDefaultOnRedactedEventReceived(
            getAllMessageNotificationsForRoomResult = { _, _ ->
                listOf(
                    mockk {
                        every { id } returns 1
                        every { notification } returns fakeNotification
                        every { tag } returns A_ROOM_ID.value
                    },
                    mockk {
                        every { id } returns 2
                        every { notification } returns fakeIncorrectNotification
                        every { tag } returns A_ROOM_ID.value
                    }
                )
            },
            displayer = FakeNotificationDisplayer(showNotificationLambda),
        )
        sut.onRedactedEventsReceived(listOf(ResolvedPushEvent.Redaction(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, null)))
        showNotificationLambda.assertions().isCalledOnce()
    }

    @Test
    fun `when thread notifications are found, try to retrieve the message`() = runTest {
        val showNotificationLambda = lambdaRecorder<String?, Int, Notification, Boolean> { tag, id, _ ->
            assertThat(tag).isEqualTo("$A_ROOM_ID|$A_THREAD_ID")
            assertThat(id).isEqualTo(1)
            true
        }
        val sut = createDefaultOnRedactedEventReceived(
            getAllMessageNotificationsForRoomResult = { _, _ ->
                listOf(
                    mockk {
                        every { id } returns 1
                        every { notification } returns fakeNotification
                        every { tag } returns "$A_ROOM_ID|$A_THREAD_ID"
                    },
                    mockk {
                        every { id } returns 2
                        every { notification } returns fakeIncorrectNotification
                        every { tag } returns A_ROOM_ID.value
                    }
                )
            },
            displayer = FakeNotificationDisplayer(showNotificationResult = showNotificationLambda),
        )
        sut.onRedactedEventsReceived(listOf(ResolvedPushEvent.Redaction(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, null)))

        showNotificationLambda.assertions().isCalledOnce()
    }

    private fun createDefaultOnRedactedEventReceived(
        getAllMessageNotificationsForRoomResult: (SessionId, RoomId) -> List<StatusBarNotification> = { _, _ -> lambdaError() },
        displayer: FakeNotificationDisplayer = FakeNotificationDisplayer(),
    ): DefaultOnRedactedEventReceived {
        val context = InstrumentationRegistry.getInstrumentation().context
        return DefaultOnRedactedEventReceived(
            activeNotificationsProvider = FakeActiveNotificationsProvider(
                getMessageNotificationsForRoomResult = { _, _, _ -> lambdaError() },
                getAllMessageNotificationsForRoomResult = getAllMessageNotificationsForRoomResult,
                getNotificationsForSessionResult = { lambdaError() },
                getMembershipNotificationForSessionResult = { lambdaError() },
                getMembershipNotificationForRoomResult = { _, _ -> lambdaError() },
                getSummaryNotificationResult = { lambdaError() },
                countResult = { lambdaError() },
            ),
            notificationDisplayer = displayer,
            context = context,
            stringProvider = FakeStringProvider(),
        )
    }
}
