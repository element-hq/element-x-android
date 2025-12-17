/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationDisplayer
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationTestTest {
    private val notificationCreator = FakeNotificationCreator()
    private val fakeNotificationDisplayer = FakeNotificationDisplayer(
        displayDiagnosticNotificationResult = lambdaRecorder { _ -> true },
        dismissDiagnosticNotificationResult = lambdaRecorder { -> }
    )

    private val notificationClickHandler = NotificationClickHandler()

    @Test
    fun `test NotificationTest notification cannot be displayed`() = runTest {
        fakeNotificationDisplayer.displayDiagnosticNotificationResult = lambdaRecorder { _ -> false }
        val sut = createNotificationTest()
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isInstanceOf(NotificationTroubleshootTestState.Status.Failure::class.java)
        }
    }

    @Test
    fun `test NotificationTest user does not click on notification`() = runTest {
        val sut = createNotificationTest()
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.WaitingForUser)
            assertThat(awaitItem().status).isInstanceOf(NotificationTroubleshootTestState.Status.Failure::class.java)
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
        }
    }

    @Test
    fun `test NotificationTest user clicks on notification`() = runTest {
        val sut = createNotificationTest()
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.WaitingForUser)
            notificationClickHandler.handleNotificationClick()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    private fun createNotificationTest(): NotificationTest {
        return NotificationTest(
            sessionId = A_SESSION_ID,
            notificationCreator = notificationCreator,
            notificationDisplayer = fakeNotificationDisplayer,
            notificationClickHandler = notificationClickHandler,
            stringProvider = FakeStringProvider(),
            enterpriseService = FakeEnterpriseService(),
        )
    }
}
