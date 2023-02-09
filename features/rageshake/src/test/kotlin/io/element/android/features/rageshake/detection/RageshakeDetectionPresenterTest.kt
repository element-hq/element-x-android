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

package io.element.android.features.rageshake.detection

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.bugreport.FakeScreenshotHolder
import io.element.android.features.rageshake.preferences.FakeRageShake
import io.element.android.features.rageshake.preferences.FakeRageshakeDataStore
import io.element.android.features.rageshake.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.screenshot.ImageResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RageshakeDetectionPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = RageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = RageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
        val presenter = RageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = RageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
    fun `present - screenshot then dismiss`() = runTest {
        val screenshotHolder = FakeScreenshotHolder(screenshotUri = null)
        val rageshake = FakeRageShake(isAvailableValue = true)
        val rageshakeDataStore = FakeRageshakeDataStore(isEnabled = true)
        val presenter = RageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = RageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
                RageshakeDetectionEvents.ProcessScreenshot(ImageResult.Error(Exception("Error")))
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
        val presenter = RageshakeDetectionPresenter(
            screenshotHolder = screenshotHolder,
            rageShake = rageshake,
            preferencesPresenter = RageshakePreferencesPresenter(
                rageshake = rageshake,
                rageshakeDataStore = rageshakeDataStore,
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
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
                RageshakeDetectionEvents.ProcessScreenshot(ImageResult.Error(Exception("Error")))
            )
            assertThat(awaitItem().showDialog).isTrue()
            initialState.eventSink.invoke(RageshakeDetectionEvents.Disable)
            skipItems(1)
            assertThat(awaitItem().showDialog).isFalse()
            assertThat(rageshakeDataStore.isEnabled().first()).isFalse()
        }
    }
}
