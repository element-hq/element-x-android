/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AdvancedSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(isDeveloperModeEnabled).isFalse()
                assertThat(isSharePresenceEnabled).isTrue()
                assertThat(mediaOptimizationState).isNull()
                assertThat(theme).isEqualTo(ThemeOption.System)
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                assertThat(mediaPreviewConfigState.setHideInviteAvatarsAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(mediaPreviewConfigState.setTimelineMediaPreviewAction).isEqualTo(AsyncAction.Uninitialized)
            }

            // After the initial state, we expect the media optimization state to be set
            with(awaitItem()) {
                assertThat(mediaOptimizationState).isInstanceOf(MediaOptimizationState.AllMedia::class.java)
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - developer mode on off`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(isDeveloperModeEnabled).isFalse()
                eventSink(AdvancedSettingsEvents.SetDeveloperModeEnabled(true))
            }
            with(awaitItem()) {
                assertThat(isDeveloperModeEnabled).isTrue()
                eventSink(AdvancedSettingsEvents.SetDeveloperModeEnabled(false))
            }
            with(awaitItem()) {
                assertThat(isDeveloperModeEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - share presence off on`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isTrue()
                eventSink(AdvancedSettingsEvents.SetSharePresenceEnabled(false))
            }
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isFalse()
                eventSink(AdvancedSettingsEvents.SetSharePresenceEnabled(true))
            }
            with(awaitItem()) {
                assertThat(isSharePresenceEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - compress media off on`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isTrue()
                eventSink(AdvancedSettingsEvents.SetCompressMedia(false))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isFalse()
                eventSink(AdvancedSettingsEvents.SetCompressMedia(true))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.AllMedia).isEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - compress images off on`() = runTest {
        val presenter = createAdvancedSettingsPresenter(
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.SelectableMediaQuality, true)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isTrue()
                eventSink(AdvancedSettingsEvents.SetCompressImages(false))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isFalse()
                eventSink(AdvancedSettingsEvents.SetCompressImages(true))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).compressImages).isTrue()
            }
        }
    }

    @Test
    fun `present - video upload quality selector`() = runTest {
        val presenter = createAdvancedSettingsPresenter(
            featureFlagService = FakeFeatureFlagService().apply {
                setFeatureEnabled(FeatureFlags.SelectableMediaQuality, true)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.STANDARD)
                eventSink(AdvancedSettingsEvents.SetVideoUploadQuality(VideoCompressionPreset.LOW))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.LOW)
                eventSink(AdvancedSettingsEvents.SetVideoUploadQuality(VideoCompressionPreset.HIGH))
            }
            with(awaitItem()) {
                assertThat((mediaOptimizationState as MediaOptimizationState.Split).videoPreset).isEqualTo(VideoCompressionPreset.HIGH)
            }
        }
    }

    @Test
    fun `present - change theme`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.System)
                eventSink(AdvancedSettingsEvents.SetTheme(ThemeOption.Dark))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.Dark)
                eventSink(AdvancedSettingsEvents.SetTheme(ThemeOption.Light))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.Light)
                eventSink(AdvancedSettingsEvents.SetTheme(ThemeOption.System))
            }
            with(awaitItem()) {
                assertThat(theme).isEqualTo(ThemeOption.System)
            }
        }
    }

    @Test
    fun `present - hide invite avatars`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore()
        val presenter = createAdvancedSettingsPresenter(mediaPreviewConfigStateStore = mediaPreviewStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
                eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(true))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isTrue()
                eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(false))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isFalse()
            }
        }
        assertThat(mediaPreviewStore.getSetHideInviteAvatarsEvents()).isEqualTo(listOf(true, false))
    }

    @Test
    fun `present - timeline media preview value`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore()
        val presenter = createAdvancedSettingsPresenter(mediaPreviewConfigStateStore = mediaPreviewStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
            }
            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
            }
        }
        assertThat(mediaPreviewStore.getSetTimelineMediaPreviewValueEvents()).isEqualTo(
            listOf(MediaPreviewValue.Off, MediaPreviewValue.Private)
        )
    }

    @Test
    fun `present - media preview state with custom initial values`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore(
            hideInviteAvatarsValue = true,
            timelineMediaPreviewValue = MediaPreviewValue.Private
        )
        val presenter = createAdvancedSettingsPresenter(mediaPreviewConfigStateStore = mediaPreviewStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.hideInviteAvatars).isTrue()
                assertThat(mediaPreviewConfigState.timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
            }
        }
    }

    @Test
    fun `present - async actions state`() = runTest {
        val mediaPreviewStore = FakeMediaPreviewConfigStateStore(
            setHideInviteAvatarsActionValue = AsyncAction.Loading,
            setTimelineMediaPreviewActionValue = AsyncAction.Success(Unit)
        )
        val presenter = createAdvancedSettingsPresenter(mediaPreviewConfigStateStore = mediaPreviewStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip until the initial data it loaded
            skipItems(1)

            with(awaitItem()) {
                assertThat(mediaPreviewConfigState.setHideInviteAvatarsAction).isEqualTo(AsyncAction.Loading)
                assertThat(mediaPreviewConfigState.setTimelineMediaPreviewAction).isEqualTo(AsyncAction.Success(Unit))
            }
        }
    }

    private fun CoroutineScope.createAdvancedSettingsPresenter(
        appPreferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreviewConfigStateStore: MediaPreviewConfigStateStore = FakeMediaPreviewConfigStateStore(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
    ) = AdvancedSettingsPresenter(
        appPreferencesStore = appPreferencesStore,
        sessionPreferencesStore = sessionPreferencesStore,
        mediaPreviewConfigStateStore = mediaPreviewConfigStateStore,
        featureFlagService = featureFlagService,
        sessionCoroutineScope = this,
    )
}
