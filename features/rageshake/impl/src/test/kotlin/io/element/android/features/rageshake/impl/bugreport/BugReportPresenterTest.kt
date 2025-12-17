/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.impl.crash.A_CRASH_DATA
import io.element.android.features.rageshake.impl.crash.CrashDataStore
import io.element.android.features.rageshake.impl.crash.FakeCrashDataStore
import io.element.android.features.rageshake.impl.screenshot.A_SCREENSHOT_URI
import io.element.android.features.rageshake.impl.screenshot.FakeScreenshotHolder
import io.element.android.features.rageshake.impl.screenshot.ScreenshotHolder
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

const val A_SHORT_DESCRIPTION = "bug!"
const val A_LONG_DESCRIPTION = "I have seen a bug!"

class BugReportPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.hasCrashLogs).isFalse()
            assertThat(initialState.formState).isEqualTo(BugReportFormState.Default)
            assertThat(initialState.sending).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.screenshotUri).isNull()
            assertThat(initialState.sendingProgress).isEqualTo(0f)
            assertThat(initialState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - set description`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_SHORT_DESCRIPTION))
            assertThat(awaitItem().submitEnabled).isTrue()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_LONG_DESCRIPTION))
            assertThat(awaitItem().submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - can contact`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetCanContact(true))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(canContact = true))
            initialState.eventSink.invoke(BugReportEvents.SetCanContact(false))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(canContact = false))
        }
    }

    @Test
    fun `present - send logs`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Since this is true by default, start by disabling
            initialState.eventSink.invoke(BugReportEvents.SetSendLog(false))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendLogs = false))
            initialState.eventSink.invoke(BugReportEvents.SetSendLog(true))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendLogs = true))
        }
    }

    @Test
    fun `present - send screenshot`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetSendScreenshot(true))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendScreenshot = true))
            initialState.eventSink.invoke(BugReportEvents.SetSendScreenshot(false))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendScreenshot = false))
        }
    }

    @Test
    fun `present - send notification settings`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetSendPushRules(true))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendPushRules = true))
            initialState.eventSink.invoke(BugReportEvents.SetSendPushRules(false))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendPushRules = false))
        }
    }

    @Test
    fun `present - reset all`() = runTest {
        val presenter = createPresenter(
            crashDataStore = FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            screenshotHolder = FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.hasCrashLogs).isTrue()
            assertThat(initialState.screenshotUri).isEqualTo(A_SCREENSHOT_URI)
            initialState.eventSink.invoke(BugReportEvents.ResetAll)
            val resetState = awaitItem()
            assertThat(resetState.hasCrashLogs).isFalse()
            // TODO Make it live assertThat(resetState.screenshotUri).isNull()
        }
    }

    @Test
    fun `present - send success`() = runTest {
        val presenter = createPresenter(
            FakeBugReporter(mode = FakeBugReporter.Mode.Success),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_LONG_DESCRIPTION))
            skipItems(1)
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(AsyncAction.Loading)
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(progressState.submitEnabled).isFalse()
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            assertThat(awaitItem().sendingProgress).isEqualTo(1f)
            skipItems(1)
            assertThat(awaitItem().sending).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - send failure`() = runTest {
        val presenter = createPresenter(
            FakeBugReporter(mode = FakeBugReporter.Mode.Failure),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_LONG_DESCRIPTION))
            skipItems(1)
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(AsyncAction.Loading)
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            // Failure
            assertThat(awaitItem().sendingProgress).isEqualTo(0f)
            assertThat((awaitItem().sending as AsyncAction.Failure).error.message).isEqualTo(A_FAILURE_REASON)
            // Reset failure
            initialState.eventSink.invoke(BugReportEvents.ClearError)
            val lastItem = awaitItem()
            assertThat(lastItem.sendingProgress).isEqualTo(0f)
            assertThat(lastItem.sending).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - send failure description too short`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_SHORT_DESCRIPTION))
            skipItems(1)
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            val errorState = awaitItem()
            assertThat(errorState.sending).isEqualTo(AsyncAction.Failure(BugReportFormError.DescriptionTooShort))
            // Reset failure
            initialState.eventSink.invoke(BugReportEvents.ClearError)
            val lastItem = awaitItem()
            assertThat(lastItem.sending).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - send cancel`() = runTest {
        val presenter = createPresenter(
            FakeBugReporter(mode = FakeBugReporter.Mode.Cancel),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_LONG_DESCRIPTION))
            skipItems(1)
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(AsyncAction.Loading)
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            // Cancelled
            assertThat(awaitItem().sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sending).isEqualTo(AsyncAction.Uninitialized)
        }
    }
}

internal fun TestScope.createPresenter(
    bugReporter: BugReporter = FakeBugReporter(),
    crashDataStore: CrashDataStore = FakeCrashDataStore(),
    screenshotHolder: ScreenshotHolder = FakeScreenshotHolder(),
) = BugReportPresenter(
    bugReporter = bugReporter,
    crashDataStore = crashDataStore,
    screenshotHolder = screenshotHolder,
    appCoroutineScope = this,
)
