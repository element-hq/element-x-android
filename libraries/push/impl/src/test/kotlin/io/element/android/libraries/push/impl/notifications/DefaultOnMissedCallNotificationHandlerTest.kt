/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.notification.FakeNotificationService
import io.element.android.libraries.matrix.test.notification.aNotificationData
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDataFactory
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.test.notifications.FakeCallNotificationEventResolver
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val dataFactory = FakeNotificationDataFactory(
            messageEventToNotificationsResult = lambdaRecorder { _, _, _ -> emptyList() }
        )
        // Create a fake matrix client provider that returns a fake matrix client with a fake notification service that returns a valid notification data
        val matrixClientProvider = FakeMatrixClientProvider(getClient = {
            val notificationService = FakeNotificationService().apply {
                givenGetNotificationResult(Result.success(aNotificationData(senderDisplayName = A_USER_NAME, senderIsNameAmbiguous = false)))
            }
            Result.success(FakeMatrixClient(notificationService = notificationService))
        })
        val defaultOnMissedCallNotificationHandler = DefaultOnMissedCallNotificationHandler(
            matrixClientProvider = matrixClientProvider,
            defaultNotificationDrawerManager = DefaultNotificationDrawerManager(
                notificationManager = mockk(relaxed = true),
                notificationRenderer = NotificationRenderer(
                    notificationDisplayer = FakeNotificationDisplayer(),
                    notificationDataFactory = dataFactory,
                ),
                appNavigationStateService = FakeAppNavigationStateService(),
                coroutineScope = backgroundScope,
                matrixClientProvider = FakeMatrixClientProvider(),
                imageLoaderHolder = FakeImageLoaderHolder(),
                activeNotificationsProvider = FakeActiveNotificationsProvider(),
            ),
            callNotificationEventResolver = FakeCallNotificationEventResolver(resolveEventLambda = { _, _, _ -> aNotifiableMessageEvent() }),
        )

        defaultOnMissedCallNotificationHandler.addMissedCallNotification(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
        )

        runCurrent()

        dataFactory.messageEventToNotificationsResult.assertions().isCalledOnce()
    }
}
