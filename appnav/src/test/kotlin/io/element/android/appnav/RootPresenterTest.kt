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

package io.element.android.appnav

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.root.RootEvents
import io.element.android.appnav.root.RootPresenter
import io.element.android.features.rageshake.impl.crash.DefaultCrashDetectionPresenter
import io.element.android.features.rageshake.impl.detection.DefaultRageshakeDetectionPresenter
import io.element.android.features.rageshake.impl.preferences.DefaultRageshakePreferencesPresenter
import io.element.android.features.rageshake.test.crash.FakeCrashDataStore
import io.element.android.features.rageshake.test.rageshake.FakeRageShake
import io.element.android.features.rageshake.test.rageshake.FakeRageshakeDataStore
import io.element.android.features.rageshake.test.screenshot.FakeScreenshotHolder
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
        val crashDetectionPresenter = DefaultCrashDetectionPresenter(
            crashDataStore = crashDataStore
        )
        val rageshakeDetectionPresenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
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
