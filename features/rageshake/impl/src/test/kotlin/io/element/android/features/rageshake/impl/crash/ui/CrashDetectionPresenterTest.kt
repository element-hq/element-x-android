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

package io.element.android.features.rageshake.impl.crash.ui

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.crash.CrashDetectionEvents
import io.element.android.features.rageshake.impl.crash.DefaultCrashDetectionPresenter
import io.element.android.features.rageshake.test.crash.A_CRASH_DATA
import io.element.android.features.rageshake.test.crash.FakeCrashDataStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CrashDetectionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state no crash`() = runTest {
        val presenter = DefaultCrashDetectionPresenter(
            FakeCrashDataStore()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - initial state crash`() = runTest {
        val presenter = DefaultCrashDetectionPresenter(
            FakeCrashDataStore(appHasCrashed = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
        }
    }

    @Test
    fun `present - reset app has crashed`() = runTest {
        val presenter = DefaultCrashDetectionPresenter(
            FakeCrashDataStore(appHasCrashed = true)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
            initialState.eventSink.invoke(CrashDetectionEvents.ResetAppHasCrashed)
            assertThat(awaitItem().crashDetected).isFalse()
        }
    }

    @Test
    fun `present - reset all crash data`() = runTest {
        val presenter = DefaultCrashDetectionPresenter(
            FakeCrashDataStore(appHasCrashed = true, crashData = A_CRASH_DATA)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.crashDetected).isTrue()
            initialState.eventSink.invoke(CrashDetectionEvents.ResetAllCrashData)
            assertThat(awaitItem().crashDetected).isFalse()
        }
    }
}
