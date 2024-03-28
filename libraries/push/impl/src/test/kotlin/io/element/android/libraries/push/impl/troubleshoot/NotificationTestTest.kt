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
import io.element.android.libraries.push.impl.notifications.fake.MockkNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.MockkNotificationDisplayer
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationTestTest {
    private val mockkNotificationCreator = MockkNotificationCreator().apply {
        givenCreateDiagnosticNotification()
    }
    private val mockkNotificationDisplayer = MockkNotificationDisplayer().apply {
        givenDisplayDiagnosticNotificationResult(true)
    }

    private val notificationClickHandler = NotificationClickHandler()

    @Test
    fun `test NotificationTest notification cannot be displayed`() = runTest {
        mockkNotificationDisplayer.givenDisplayDiagnosticNotificationResult(false)
        val sut = createNotificationTest()
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isInstanceOf(NotificationTroubleshootTestState.Status.Failure::class.java)
        }
    }

    @Test
    fun `test NotificationTest user does not click on notification`() = runTest {
        val sut = createNotificationTest()
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.WaitingForUser)
            assertThat(awaitItem().status).isInstanceOf(NotificationTroubleshootTestState.Status.Failure::class.java)
        }
    }

    @Test
    fun `test NotificationTest user clicks on notification`() = runTest {
        val sut = createNotificationTest()
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.WaitingForUser)
            notificationClickHandler.handleNotificationClick()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    private fun createNotificationTest(): NotificationTest {
        return NotificationTest(
            notificationCreator = mockkNotificationCreator.instance,
            notificationDisplayer = mockkNotificationDisplayer.instance,
            notificationClickHandler = notificationClickHandler,
            stringProvider = FakeStringProvider(),
        )
    }
}
