/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultSummaryGroupMessageCreatorTest {
    @Test
    fun `process notifications`() = runTest {
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
        )

        notificationCreator.createSummaryListNotificationResult.assertions()
            .isCalledOnce()
            .with(any(), nonNull(), any(), any())

        // Set from the events included
        @Suppress("DEPRECATION")
        assertThat(result.priority).isEqualTo(NotificationCompat.PRIORITY_DEFAULT)
    }
}
