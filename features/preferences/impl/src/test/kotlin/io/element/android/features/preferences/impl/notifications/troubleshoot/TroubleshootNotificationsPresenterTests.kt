/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.preferences.impl.notifications.troubleshoot

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TroubleshootNotificationsPresenterTests {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createTroubleshootNotificationsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.testSuiteState.tests).isEmpty()
            assertThat(initialState.testSuiteState.mainState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - start test`() = runTest {
        val troubleshootTestSuite = createTroubleshootTestSuite(
            tests = setOf(FakeNotificationTroubleshootTest())
        )
        val presenter = createTroubleshootNotificationsPresenter(
            troubleshootTestSuite = troubleshootTestSuite,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(TroubleshootNotificationsEvents.StartTests)
            skipItems(1)
            val stateAfterStart = awaitItem()
            assertThat(stateAfterStart.testSuiteState.mainState).isEqualTo(AsyncAction.Loading)
        }
    }

    @Test
    fun `present - start failed test`() = runTest {
        val troubleshootTestSuite = createTroubleshootTestSuite(
            tests = setOf(
                FakeNotificationTroubleshootTest(
                    firstStatus = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = false)
                )
            )
        )
        val presenter = createTroubleshootNotificationsPresenter(
            troubleshootTestSuite = troubleshootTestSuite,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(TroubleshootNotificationsEvents.RetryFailedTests)
            skipItems(1)
            val stateAfterStart = awaitItem()
            assertThat(stateAfterStart.testSuiteState.mainState).isEqualTo(AsyncAction.Loading)
        }
    }

    @Test
    fun `present - quick fix test`() = runTest {
        val troubleshootTestSuite = createTroubleshootTestSuite(
            tests = setOf(
                FakeNotificationTroubleshootTest(
                    firstStatus = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = false)
                )
            )
        )
        val presenter = createTroubleshootNotificationsPresenter(
            troubleshootTestSuite = troubleshootTestSuite,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.testSuiteState.mainState).isInstanceOf(AsyncAction.Failure::class.java)
            initialState.eventSink(TroubleshootNotificationsEvents.QuickFix(0))
            val stateAfterStart = awaitItem()
            assertThat(stateAfterStart.testSuiteState.mainState).isEqualTo(AsyncAction.Loading)
        }
    }

    private fun createTroubleshootTestSuite(
        tests: Set<NotificationTroubleshootTest> = emptySet(),
        currentPushProvider: String? = null,
    ): TroubleshootTestSuite {
        return TroubleshootTestSuite(
            notificationTroubleshootTests = tests,
            getCurrentPushProvider = FakeGetCurrentPushProvider(currentPushProvider),
        )
    }

    private fun createTroubleshootNotificationsPresenter(
        troubleshootTestSuite: TroubleshootTestSuite = createTroubleshootTestSuite(),
    ): TroubleshootNotificationsPresenter {
        return TroubleshootNotificationsPresenter(
            troubleshootTestSuite = troubleshootTestSuite,
        )
    }
}
