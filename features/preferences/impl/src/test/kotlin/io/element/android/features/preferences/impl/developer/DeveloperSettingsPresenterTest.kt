/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.logout.test.FakeLogoutUseCase
import io.element.android.features.preferences.impl.tasks.FakeClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.FakeComputeCacheSizeUseCase
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeveloperSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - ensures initial state is correct`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.features).isEmpty()
            assertThat(initialState.clearCacheAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.cacheSize).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.customElementCallBaseUrlState).isNotNull()
            assertThat(initialState.customElementCallBaseUrlState.baseUrl).isNull()
            assertThat(initialState.isSimpleSlidingSyncEnabled).isFalse()
            assertThat(initialState.hideImagesAndVideos).isFalse()
            val loadedState = awaitItem()
            assertThat(loadedState.rageshakeState.isEnabled).isFalse()
            assertThat(loadedState.rageshakeState.isSupported).isTrue()
            assertThat(loadedState.rageshakeState.sensitivity).isEqualTo(0.3f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - ensures feature list is loaded`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitLastSequentialItem()
            val numberOfModifiableFeatureFlags = FeatureFlags.entries.count { it.isFinished.not() }
            assertThat(state.features).hasSize(numberOfModifiableFeatureFlags)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - ensures Room directory search is not present on release Google Play builds`() = runTest {
        val buildMeta = aBuildMeta(buildType = BuildType.RELEASE, flavorDescription = "GooglePlay")
        val presenter = createDeveloperSettingsPresenter(buildMeta = buildMeta)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitLastSequentialItem()
            assertThat(state.features).doesNotContain(FeatureFlags.RoomDirectorySearch)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - ensures state is updated when enabled feature event is triggered`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val stateBeforeEvent = awaitItem()
            val featureBeforeEvent = stateBeforeEvent.features.first()
            stateBeforeEvent.eventSink(DeveloperSettingsEvents.UpdateEnabledFeature(featureBeforeEvent, !featureBeforeEvent.isEnabled))
            val stateAfterEvent = awaitItem()
            val featureAfterEvent = stateAfterEvent.features.first()
            assertThat(featureBeforeEvent.key).isEqualTo(featureAfterEvent.key)
            assertThat(featureBeforeEvent.isEnabled).isNotEqualTo(featureAfterEvent.isEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clear cache`() = runTest {
        val clearCacheUseCase = FakeClearCacheUseCase()
        val presenter = createDeveloperSettingsPresenter(clearCacheUseCase = clearCacheUseCase)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(clearCacheUseCase.executeHasBeenCalled).isFalse()
            initialState.eventSink(DeveloperSettingsEvents.ClearCache)
            val stateAfterEvent = awaitItem()
            assertThat(stateAfterEvent.clearCacheAction).isInstanceOf(AsyncData.Loading::class.java)
            skipItems(1)
            assertThat(awaitItem().clearCacheAction).isInstanceOf(AsyncData.Success::class.java)
            assertThat(clearCacheUseCase.executeHasBeenCalled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - custom element call base url`() = runTest {
        val preferencesStore = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferencesStore)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.customElementCallBaseUrlState.baseUrl).isNull()
            initialState.eventSink(DeveloperSettingsEvents.SetCustomElementCallBaseUrl("https://call.element.ahoy"))
            val updatedItem = awaitItem()
            assertThat(updatedItem.customElementCallBaseUrlState.baseUrl).isEqualTo("https://call.element.ahoy")
            assertThat(updatedItem.customElementCallBaseUrlState.defaultUrl).isEqualTo(ElementCallConfig.DEFAULT_BASE_URL)
        }
    }

    @Test
    fun `present - custom element call base url validator needs at least an HTTP scheme and host`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val urlValidator = awaitLastSequentialItem().customElementCallBaseUrlState.validator
            assertThat(urlValidator("")).isTrue() // We allow empty string to clear the value and use the default one
            assertThat(urlValidator("test")).isFalse()
            assertThat(urlValidator("http://")).isFalse()
            assertThat(urlValidator("geo://test")).isFalse()
            assertThat(urlValidator("https://call.element.io")).isTrue()
        }
    }

    @Test
    fun `present - toggling simplified sliding sync changes the preferences and logs out the user`() = runTest {
        val logoutCallRecorder = lambdaRecorder<Boolean, String?> { "" }
        val logoutUseCase = FakeLogoutUseCase(logoutLambda = logoutCallRecorder)
        val preferences = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferences, logoutUseCase = logoutUseCase)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isSimpleSlidingSyncEnabled).isFalse()

            initialState.eventSink(DeveloperSettingsEvents.SetSimplifiedSlidingSyncEnabled(true))
            assertThat(awaitItem().isSimpleSlidingSyncEnabled).isTrue()
            assertThat(preferences.isSimplifiedSlidingSyncEnabledFlow().first()).isTrue()
            logoutCallRecorder.assertions().isCalledOnce()

            initialState.eventSink(DeveloperSettingsEvents.SetSimplifiedSlidingSyncEnabled(false))
            assertThat(awaitItem().isSimpleSlidingSyncEnabled).isFalse()
            assertThat(preferences.isSimplifiedSlidingSyncEnabledFlow().first()).isFalse()
            logoutCallRecorder.assertions().isCalledExactly(times = 2)
        }
    }

    @Test
    fun `present - toggling hide image and video`() = runTest {
        val preferences = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferences)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.hideImagesAndVideos).isFalse()
            initialState.eventSink(DeveloperSettingsEvents.SetHideImagesAndVideos(true))
            assertThat(awaitItem().hideImagesAndVideos).isTrue()
            assertThat(preferences.doesHideImagesAndVideosFlow().first()).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.SetHideImagesAndVideos(false))
            assertThat(awaitItem().hideImagesAndVideos).isFalse()
            assertThat(preferences.doesHideImagesAndVideosFlow().first()).isFalse()
        }
    }

    private fun createDeveloperSettingsPresenter(
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
        cacheSizeUseCase: FakeComputeCacheSizeUseCase = FakeComputeCacheSizeUseCase(),
        clearCacheUseCase: FakeClearCacheUseCase = FakeClearCacheUseCase(),
        preferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        buildMeta: BuildMeta = aBuildMeta(),
        logoutUseCase: FakeLogoutUseCase = FakeLogoutUseCase(logoutLambda = { "" })
    ): DeveloperSettingsPresenter {
        return DeveloperSettingsPresenter(
            featureFlagService = featureFlagService,
            computeCacheSizeUseCase = cacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
            rageshakePresenter = { aRageshakePreferencesState() },
            appPreferencesStore = preferencesStore,
            buildMeta = buildMeta,
            logoutUseCase = logoutUseCase,
        )
    }
}
