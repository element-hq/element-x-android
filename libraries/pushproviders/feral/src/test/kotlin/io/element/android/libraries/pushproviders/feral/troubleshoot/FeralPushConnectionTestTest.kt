/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.pushproviders.feral.FakeFeralPushServiceController
import io.element.android.libraries.pushproviders.feral.FeralPushConnectionMonitor
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.libraries.troubleshoot.test.FakeNotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeralPushConnectionTestTest {
    private fun createTest(
        monitor: FeralPushConnectionMonitor = FeralPushConnectionMonitor(),
        serviceController: FakeFeralPushServiceController = FakeFeralPushServiceController(),
        batteryOptimization: FakeFeralPushBatteryOptimization = FakeFeralPushBatteryOptimization(),
    ) = FeralPushConnectionTest(
        sessionId = A_SESSION_ID,
        monitor = monitor,
        serviceController = serviceController,
        batteryOptimization = batteryOptimization,
        stringProvider = FakeStringProvider(),
    )

    @Test
    fun `relevant only for the Feral provider`() {
        val sut = createTest()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "Feral"))).isTrue()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "UnifiedPush"))).isFalse()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = null))).isFalse()
    }

    @Test
    fun `success when the service runs, the session is connected and battery optimisation is off`() = runTest {
        val monitor = FeralPushConnectionMonitor().apply {
            setServiceRunning(true)
            setConnected(A_SESSION_ID, true)
        }
        val sut = createTest(monitor = monitor)
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `non critical failure when battery optimisation is on, fixed by requesting the exemption`() = runTest {
        val monitor = FeralPushConnectionMonitor().apply {
            setServiceRunning(true)
            setConnected(A_SESSION_ID, true)
        }
        val battery = FakeFeralPushBatteryOptimization(ignoring = false)
        val controller = FakeFeralPushServiceController()
        val sut = createTest(monitor = monitor, batteryOptimization = battery, serviceController = controller)
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true, isCritical = false))
            sut.quickFix(this@runTest, FakeNotificationTroubleshootNavigator())
            assertThat(battery.requestCalls).isEqualTo(1)
            assertThat(controller.startIfRegisteredCalls).isEqualTo(0)
        }
    }

    @Test
    fun `failure when the service is not running, fixed by starting it`() = runTest {
        val controller = FakeFeralPushServiceController()
        val sut = createTest(serviceController = controller)
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            sut.quickFix(this@runTest, FakeNotificationTroubleshootNavigator())
            assertThat(controller.startIfRegisteredCalls).isEqualTo(1)
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `failure when the service runs but the session is not connected`() = runTest {
        val monitor = FeralPushConnectionMonitor().apply { setServiceRunning(true) }
        val sut = createTest(monitor = monitor)
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
        }
    }
}
