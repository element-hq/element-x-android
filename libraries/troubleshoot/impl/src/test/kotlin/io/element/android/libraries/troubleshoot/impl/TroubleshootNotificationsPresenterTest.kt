/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TroubleshootNotificationsPresenterTest {
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
            analyticsService = FakeAnalyticsService(),
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
