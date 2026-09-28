/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.media

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MediaSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createMediaSettingsPresenter().test {
            assertThat(awaitItem().mediaOptimizationState).isNull()
            // After the initial state, we expect the media optimization state to be set
            with(awaitItem()) {
                assertThat(mediaOptimizationState).isEqualTo(MediaOptimizationState.AllMedia(isEnabled = true))
            }
        }
    }

    @Test
    fun `present - compress media off on`() = runTest {
        createMediaSettingsPresenter().test {
            // Skip until the initial data is loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isTrue()
                eventSink(MediaSettingsEvent.SetCompressMedia(false))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isFalse()
                eventSink(MediaSettingsEvent.SetCompressMedia(true))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - split media optimization state when the feature flag is enabled`() = runTest {
        createMediaSettingsPresenter(
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.SelectableMediaQuality, true)
            }
        ).test {
            // Skip until the initial data is loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isTrue()
                eventSink(MediaSettingsEvent.SetCompressMedia(false))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isFalse()
                eventSink(MediaSettingsEvent.SetCompressMedia(true))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isTrue()
            }
        }
    }

    @Test
    fun `present - video upload quality selector`() = runTest {
        createMediaSettingsPresenter(
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.SelectableMediaQuality, true)
            }
        ).test {
            // Skip until the initial data is loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.STANDARD)
                eventSink(MediaSettingsEvent.SetVideoUploadQuality(VideoCompressionPreset.LOW))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.LOW)
                eventSink(MediaSettingsEvent.SetVideoUploadQuality(VideoCompressionPreset.HIGH))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.HIGH)
            }
        }
    }

    private fun CoroutineScope.createMediaSettingsPresenter(
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
    ) = MediaSettingsPresenter(
        sessionPreferencesStore = sessionPreferencesStore,
        featureFlagService = featureFlagService,
        sessionCoroutineScope = this,
    )
}
