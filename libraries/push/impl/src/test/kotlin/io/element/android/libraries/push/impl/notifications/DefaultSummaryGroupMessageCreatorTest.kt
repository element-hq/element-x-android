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
import androidx.core.app.NotificationCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.nonNull
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultSummaryGroupMessageCreatorTest {
    @Test
    fun `process notifications with complete format`() = runTest {
        val notificationCreator = FakeNotificationCreator()
        val summaryCreator = DefaultSummaryGroupMessageCreator(
            stringProvider = FakeStringProvider(),
            notificationCreator = notificationCreator,
        )

        val result = summaryCreator.createSummaryNotification(
            currentUser = aMatrixUser(),
            roomNotifications = listOf(
                RoomNotification(
                    notification = Notification(),
                    roomId = A_ROOM_ID,
                    summaryLine = "",
                    messageCount = 1,
                    latestTimestamp = A_FAKE_TIMESTAMP + 10,
                    shouldBing = true,
                )
            ),
            invitationNotifications = emptyList(),
            simpleNotifications = emptyList(),
            fallbackNotifications = emptyList(),
            useCompleteNotificationFormat = true,
        )

        notificationCreator.createSummaryListNotificationResult.assertions()
            .isCalledOnce()
            .with(any(), nonNull(), any(), any(), any())

        // Set from the events included
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_DEFAULT)
    }

    @Test
    fun `process notifications without complete format`() = runTest {
        val notificationCreator = FakeNotificationCreator()
        val summaryCreator = DefaultSummaryGroupMessageCreator(
            stringProvider = FakeStringProvider(),
            notificationCreator = notificationCreator,
        )

        val result = summaryCreator.createSummaryNotification(
            currentUser = aMatrixUser(),
            roomNotifications = listOf(
                RoomNotification(
                    notification = Notification(),
                    roomId = A_ROOM_ID,
                    summaryLine = "",
                    messageCount = 1,
                    latestTimestamp = A_FAKE_TIMESTAMP + 10,
                    shouldBing = true,
                )
            ),
            invitationNotifications = emptyList(),
            simpleNotifications = emptyList(),
            fallbackNotifications = emptyList(),
            useCompleteNotificationFormat = false,
        )

        notificationCreator.createSummaryListNotificationResult.assertions()
            .isCalledOnce()
            .with(any(), value(null), any(), any(), any())

        // Set from the events included
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_DEFAULT)
    }
}
