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

package io.element.android.libraries.push.impl.troubleshoot

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import io.element.android.libraries.push.test.FakePushService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
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
            clock = FakeSystemClock()
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            assertThat(lastItem.description).contains("timeout")
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
            clock = FakeSystemClock()
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            assertThat(lastItem.description).contains("rejected")
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
            clock = FakeSystemClock()
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            assertThat(lastItem.description).contains("cannot test push")
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
            clock = FakeSystemClock()
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
            clock = FakeSystemClock()
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
