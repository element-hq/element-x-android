/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PushLoopbackTestTest {
    @Test
    fun `test PushLoopbackTest timeout - push is not received`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val sut = PushLoopbackTest(
            pushService = FakePushService(),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
        }
    }

    @Test
    fun `test PushLoopbackTest PusherRejected error`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val sut = PushLoopbackTest(
            pushService = FakePushService(
                testPushBlock = {
                    throw PushGatewayFailure.PusherRejected()
                }
            ),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
        }
    }

    @Test
    fun `test PushLoopbackTest PusherRejected error with quick fix`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val rotateTokenLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val sut = PushLoopbackTest(
            pushService = FakePushService(
                testPushBlock = {
                    throw PushGatewayFailure.PusherRejected()
                },
                currentPushProvider = {
                    FakePushProvider(
                        canRotateTokenResult = { true },
                        rotateTokenLambda = rotateTokenLambda,
                    )
                }
            ),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(true))
            sut.quickFix(this)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(true))
            rotateTokenLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `test PushLoopbackTest setup error`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val sut = PushLoopbackTest(
            pushService = FakePushService(
                testPushBlock = { false }
            ),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
        }
    }

    @Test
    fun `test PushLoopbackTest other error`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val sut = PushLoopbackTest(
            pushService = FakePushService(
                testPushBlock = {
                    throw AN_EXCEPTION
                }
            ),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            assertThat(lastItem.description).contains(A_FAILURE_REASON)
        }
    }

    @Test
    fun `test PushLoopbackTest push is received`() = runTest {
        val diagnosticPushHandler = DiagnosticPushHandler()
        val sut = PushLoopbackTest(
            pushService = FakePushService(testPushBlock = {
                diagnosticPushHandler.handlePush()
                true
            }),
            diagnosticPushHandler = diagnosticPushHandler,
            clock = FakeSystemClock(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }
}
