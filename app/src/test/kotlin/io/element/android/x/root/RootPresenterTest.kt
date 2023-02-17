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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.x.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.crash.ui.CrashDetectionPresenter
import io.element.android.features.rageshake.detection.RageshakeDetectionPresenter
import io.element.android.features.rageshake.preferences.RageshakePreferencesPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isShowkaseButtonVisible).isTrue()
        }
    }

    @Test
    fun `present - hide showkase button`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isShowkaseButtonVisible).isTrue()
            initialState.eventSink.invoke(RootEvents.HideShowkaseButton)
            assertThat(awaitItem().isShowkaseButtonVisible).isFalse()
        }
    }

    private fun createPresenter(): RootPresenter {
        val crashDataStore = FakeCrashDataStore()
        val rageshakeDataStore = FakeRageshakeDataStore()
        val rageshake = FakeRageShake()
        val screenshotHolder = FakeScreenshotHolder()
        val crashDetectionPresenter = CrashDetectionPresenter(
            crashDataStore = crashDataStore
        )
        val rageshakeDetectionPresenter = RageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = RageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        return RootPresenter(
            crashDetectionPresenter = crashDetectionPresenter,
            rageshakeDetectionPresenter = rageshakeDetectionPresenter,
        )
    }
}
