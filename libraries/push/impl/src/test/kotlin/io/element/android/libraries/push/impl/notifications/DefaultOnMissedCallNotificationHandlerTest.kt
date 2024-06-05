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

import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDataFactory
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultOnMissedCallNotificationHandlerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addMissedCallNotification - should add missed call notification`() = runTest {
        val childScope = CoroutineScope(coroutineContext + SupervisorJob())
        val dataFactory = FakeNotificationDataFactory(
            callEventToNotificationsResult = lambdaRecorder { _, _ -> emptyList() }
        )
        val defaultOnMissedCallNotificationHandler = DefaultOnMissedCallNotificationHandler(
            defaultNotificationDrawerManager = DefaultNotificationDrawerManager(
                notificationManager = mockk(relaxed = true),
                notificationRenderer = NotificationRenderer(
                    notificationDisplayer = FakeNotificationDisplayer(),
                    notificationDataFactory = dataFactory,
                ),
                appNavigationStateService = FakeAppNavigationStateService(),
                coroutineScope = childScope,
                matrixClientProvider = FakeMatrixClientProvider(),
                imageLoaderHolder = FakeImageLoaderHolder(),
                activeNotificationsProvider = FakeActiveNotificationsProvider(),
            ),
            coroutineScope = childScope,
            stringProvider = FakeStringProvider(),
        )

        defaultOnMissedCallNotificationHandler.addMissedCallNotification(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID_2,
            senderName = "senderName",
            roomName = "roomName",
            timestamp = 0L,
            avatarUrl = "avatarUrl"
        )

        runCurrent()

        dataFactory.callEventToNotificationsResult.assertions().isCalledOnce()

        // Cancel the coroutine scope so the test can finish
        childScope.cancel()
    }
}
