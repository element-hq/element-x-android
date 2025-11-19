/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.unregistration

import android.app.Notification
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.element.android.appconfig.NotificationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultServiceUnregisteredHandlerTest {
    @Test
    fun `handle will create a notification and render it`() = runTest {
        val notification = A_NOTIFICATION
        val createUnregistrationNotificationResult = lambdaRecorder<NotificationAccountParams, Notification> { notification }
        val displayUnregistrationNotificationResult = lambdaRecorder<Notification, Boolean> { true }
        val sut = createDefaultServiceUnregisteredHandler(
            notificationCreator = FakeNotificationCreator(
                createUnregistrationNotificationResult = createUnregistrationNotificationResult,
            ),
            notificationDisplayer = FakeNotificationDisplayer(
                displayUnregistrationNotificationResult = displayUnregistrationNotificationResult,
            )
        )
        sut.handle(A_SESSION_ID)
        createUnregistrationNotificationResult.assertions().isCalledOnce().with(
            value(
                NotificationAccountParams(
                    MatrixUser(
                        userId = A_SESSION_ID,
                        displayName = null,
                        avatarUrl = null,
                    ),
                    color = NotificationConfig.NOTIFICATION_ACCENT_COLOR,
                    showSessionId = false,
                )
            )
        )
        displayUnregistrationNotificationResult.assertions().isCalledOnce().with(
            value(notification)
        )
    }

    @Test
    fun `handle will create a notification and render it - custom color and multi accounts`() = runTest {
        val notification = A_NOTIFICATION
        val createUnregistrationNotificationResult = lambdaRecorder<NotificationAccountParams, Notification> { notification }
        val displayUnregistrationNotificationResult = lambdaRecorder<Notification, Boolean> { true }
        val sut = createDefaultServiceUnregisteredHandler(
            enterpriseService = FakeEnterpriseService(
                initialBrandColor = Color.Red,
            ),
            notificationCreator = FakeNotificationCreator(
                createUnregistrationNotificationResult = createUnregistrationNotificationResult,
            ),
            notificationDisplayer = FakeNotificationDisplayer(
                displayUnregistrationNotificationResult = displayUnregistrationNotificationResult,
            ),
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(sessionId = A_SESSION_ID_2.value),
                )
            )
        )
        sut.handle(A_SESSION_ID)
        createUnregistrationNotificationResult.assertions().isCalledOnce().with(
            value(
                NotificationAccountParams(
                    MatrixUser(
                        userId = A_SESSION_ID,
                        displayName = null,
                        avatarUrl = null,
                    ),
                    color = Color.Red.toArgb(),
                    showSessionId = true,
                )
            )
        )
        displayUnregistrationNotificationResult.assertions().isCalledOnce().with(
            value(notification)
        )
    }

    private fun createDefaultServiceUnregisteredHandler(
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
        notificationCreator: NotificationCreator = FakeNotificationCreator(),
        notificationDisplayer: NotificationDisplayer = FakeNotificationDisplayer(),
        sessionStore: SessionStore = InMemorySessionStore(),
    ) = DefaultServiceUnregisteredHandler(
        enterpriseService = enterpriseService,
        notificationCreator = notificationCreator,
        notificationDisplayer = notificationDisplayer,
        sessionStore = sessionStore,
    )
}
