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

package io.element.android.features.rageshake.impl.detection

import android.graphics.Bitmap
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.detection.RageshakeDetectionEvents
import io.element.android.features.rageshake.api.screenshot.ImageResult
import io.element.android.features.rageshake.impl.preferences.DefaultRageshakePreferencesPresenter
import io.element.android.features.rageshake.test.rageshake.FakeRageShake
import io.element.android.features.rageshake.test.rageshake.FakeRageshakeDataStore
import io.element.android.features.rageshake.test.screenshot.FakeScreenshotHolder
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class RageshakeDetectionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private lateinit var aBitmap: Bitmap

        @BeforeClass
        @JvmStatic
        fun initBitmap() {
            aBitmap = mockk()
        }
    }

    @Test
    fun `present - initial state`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.takeScreenshot).isFalse()
            assertThat(initialState.showDialog).isFalse()
            assertThat(initialState.isStarted).isFalse()
        }
    }

    @Test
    fun `present - start and stop detection`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(RageshakeDetectionEvents.StartDetection)
            assertThat(awaitItem().isStarted).isTrue()
            initialState.eventSink.invoke(RageshakeDetectionEvents.StopDetection)
            assertThat(awaitItem().isStarted).isFalse()
        }
    }

    @Test
    fun `present - screenshot with success then dismiss`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isStarted).isFalse()
            initialState.eventSink.invoke(RageshakeDetectionEvents.StartDetection)
            assertThat(awaitItem().isStarted).isTrue()
            rageshake.triggerPhoneRageshake()
            assertThat(awaitItem().takeScreenshot).isTrue()
            initialState.eventSink.invoke(
                RageshakeDetectionEvents.ProcessScreenshot(ImageResult.Success(aBitmap))
            )
            assertThat(awaitItem().showDialog).isTrue()
            initialState.eventSink.invoke(RageshakeDetectionEvents.Dismiss)
            val finalState = awaitItem()
            assertThat(finalState.showDialog).isFalse()
            assertThat(rageshakeDataStore.isEnabled().first()).isTrue()
        }
    }

    @Test
    fun `present - screenshot with error then dismiss`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isStarted).isFalse()
            initialState.eventSink.invoke(RageshakeDetectionEvents.StartDetection)
            assertThat(awaitItem().isStarted).isTrue()
            rageshake.triggerPhoneRageshake()
            assertThat(awaitItem().takeScreenshot).isTrue()
            initialState.eventSink.invoke(
                RageshakeDetectionEvents.ProcessScreenshot(ImageResult.Error(AN_EXCEPTION))
            )
            assertThat(awaitItem().showDialog).isTrue()
            initialState.eventSink.invoke(RageshakeDetectionEvents.Dismiss)
            val finalState = awaitItem()
            assertThat(finalState.showDialog).isFalse()
            assertThat(rageshakeDataStore.isEnabled().first()).isTrue()
        }
    }

    @Test
    fun `present - screenshot then disable`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = DefaultRageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = DefaultRageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isStarted).isFalse()
            initialState.eventSink.invoke(RageshakeDetectionEvents.StartDetection)
            assertThat(awaitItem().isStarted).isTrue()
            rageshake.triggerPhoneRageshake()
            assertThat(awaitItem().takeScreenshot).isTrue()
            initialState.eventSink.invoke(
                RageshakeDetectionEvents.ProcessScreenshot(ImageResult.Success(aBitmap))
            )
            assertThat(awaitItem().showDialog).isTrue()
            initialState.eventSink.invoke(RageshakeDetectionEvents.Disable)
            skipItems(1)
            assertThat(awaitItem().showDialog).isFalse()
            assertThat(rageshakeDataStore.isEnabled().first()).isFalse()
        }
    }
}
