/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.features.rageshake.impl.rageshake.FakeRageShake
import io.element.android.features.rageshake.impl.rageshake.FakeRageshakeDataStore
import io.element.android.features.rageshake.impl.screenshot.FakeScreenshotHolder
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
                rageshakeFeatureAvailability = { flowOf(true) },
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
                rageshakeFeatureAvailability = { flowOf(true) },
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
                rageshakeFeatureAvailability = { flowOf(true) },
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
                rageshakeFeatureAvailability = { flowOf(true) },
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
                rageshakeFeatureAvailability = { flowOf(true) },
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
