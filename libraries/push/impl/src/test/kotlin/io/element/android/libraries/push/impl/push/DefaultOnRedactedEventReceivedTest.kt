/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.service.notification.StatusBarNotification
import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.lambdaError
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultOnRedactedEventReceivedTest {
    @Test
    fun `when no notifications are found, nothing happen`() = runTest {
        val sut = createDefaultOnRedactedEventReceived(
            getMessageNotificationsForRoomResult = { _, _ -> emptyList() }
        )
        sut.onRedactedEventsReceived(listOf(ResolvedPushEvent.Redaction(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, null)))
    }

    @Test
    fun `when a notification is found, try to retrieve the message`() = runTest {
        val sut = createDefaultOnRedactedEventReceived(
            getMessageNotificationsForRoomResult = { _, _ ->
                listOf(
                    mockk {
                        every { notification } returns mockk {}
                    }
                )
            }
        )
        sut.onRedactedEventsReceived(listOf(ResolvedPushEvent.Redaction(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, null)))
    }

    private fun TestScope.createDefaultOnRedactedEventReceived(
        getMessageNotificationsForRoomResult: (SessionId, RoomId) -> List<StatusBarNotification> = { _, _ -> lambdaError() },
    ): DefaultOnRedactedEventReceived {
        val context = InstrumentationRegistry.getInstrumentation().context
        return DefaultOnRedactedEventReceived(
            activeNotificationsProvider = FakeActiveNotificationsProvider(
                getMessageNotificationsForRoomResult = getMessageNotificationsForRoomResult,
                getNotificationsForSessionResult = { lambdaError() },
                getMembershipNotificationForSessionResult = { lambdaError() },
                getMembershipNotificationForRoomResult = { _, _ -> lambdaError() },
                getSummaryNotificationResult = { lambdaError() },
                countResult = { lambdaError() },
            ),
            notificationDisplayer = FakeNotificationDisplayer(),
            coroutineScope = this,
            context = context,
            stringProvider = FakeStringProvider(),
        )
    }
}
