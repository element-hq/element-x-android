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

package io.element.android.features.rageshake.impl.bugreport

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.test.crash.A_CRASH_DATA
import io.element.android.features.rageshake.test.crash.FakeCrashDataStore
import io.element.android.features.rageshake.test.screenshot.A_SCREENSHOT_URI
import io.element.android.features.rageshake.test.screenshot.FakeScreenshotHolder
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_FAILURE_REASON
import kotlinx.coroutines.test.runTest
import org.junit.Test

const val A_SHORT_DESCRIPTION = "bug!"
const val A_LONG_DESCRIPTION = "I have seen a bug!"

class BugReportPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.hasCrashLogs).isFalse()
            assertThat(initialState.formState).isEqualTo(BugReportFormState.Default)
            assertThat(initialState.sending).isEqualTo(Async.Uninitialized)
            assertThat(initialState.screenshotUri).isNull()
            assertThat(initialState.sendingProgress).isEqualTo(0f)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - set description`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_SHORT_DESCRIPTION))
            assertThat(awaitItem().submitEnabled).isFalse()
            initialState.eventSink.invoke(BugReportEvents.SetDescription(A_LONG_DESCRIPTION))
            assertThat(awaitItem().submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - can contact`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
    fun `present - send crash logs`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Since this is true by default, start by disabling
            initialState.eventSink.invoke(BugReportEvents.SetSendCrashLog(false))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendCrashLogs = false))
            initialState.eventSink.invoke(BugReportEvents.SetSendCrashLog(true))
            assertThat(awaitItem().formState).isEqualTo(BugReportFormState.Default.copy(sendCrashLogs = true))
        }
    }

    @Test
    fun `present - send logs`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(),
            FakeScreenshotHolder(),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
    fun `present - reset all`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
        val presenter = BugReportPresenter(
            FakeBugReporter(mode = FakeBugReporterMode.Success),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(Async.Loading(null))
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(progressState.submitEnabled).isFalse()
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            assertThat(awaitItem().sendingProgress).isEqualTo(1f)
            skipItems(1)
            assertThat(awaitItem().sending).isEqualTo(Async.Success(Unit))
        }
    }

    @Test
    fun `present - send failure`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(mode = FakeBugReporterMode.Failure),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(Async.Loading(null))
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            // Failure
            assertThat(awaitItem().sendingProgress).isEqualTo(0f)
            assertThat((awaitItem().sending as Async.Failure).exception.message).isEqualTo(A_FAILURE_REASON)
            // Reset failure
            initialState.eventSink.invoke(BugReportEvents.ClearError)
            val lastItem = awaitItem()
            assertThat(lastItem.sendingProgress).isEqualTo(0f)
            assertThat(lastItem.sending).isInstanceOf(Async.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - send cancel`() = runTest {
        val presenter = BugReportPresenter(
            FakeBugReporter(mode = FakeBugReporterMode.Cancel),
            FakeCrashDataStore(crashData = A_CRASH_DATA, appHasCrashed = true),
            FakeScreenshotHolder(screenshotUri = A_SCREENSHOT_URI),
            this,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(BugReportEvents.SendBugReport)
            skipItems(1)
            val progressState = awaitItem()
            assertThat(progressState.sending).isEqualTo(Async.Loading(null))
            assertThat(progressState.sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sendingProgress).isEqualTo(0.5f)
            // Cancelled
            assertThat(awaitItem().sendingProgress).isEqualTo(0f)
            assertThat(awaitItem().sending).isEqualTo(Async.Uninitialized)
        }
    }
}
