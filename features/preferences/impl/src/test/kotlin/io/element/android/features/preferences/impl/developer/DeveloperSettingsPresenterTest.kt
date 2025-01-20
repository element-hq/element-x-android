/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.developer

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.logout.test.FakeLogoutUseCase
import io.element.android.features.preferences.impl.tasks.FakeClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.FakeComputeCacheSizeUseCase
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeveloperSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - ensures initial states are correct`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.features).isEmpty()
                assertThat(state.clearCacheAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.cacheSize).isEqualTo(AsyncData.Uninitialized)
                assertThat(state.customElementCallBaseUrlState).isNotNull()
                assertThat(state.customElementCallBaseUrlState.baseUrl).isNull()
                assertThat(state.isSimpleSlidingSyncEnabled).isFalse()
                assertThat(state.hideImagesAndVideos).isFalse()
                assertThat(state.rageshakeState.isEnabled).isFalse()
                assertThat(state.rageshakeState.isSupported).isTrue()
                assertThat(state.rageshakeState.sensitivity).isEqualTo(0.3f)
            }
            awaitItem().also { state ->
                assertThat(state.features).isNotEmpty()
                val numberOfModifiableFeatureFlags = FeatureFlags.entries.count { it.isFinished.not() }
                assertThat(state.features).hasSize(numberOfModifiableFeatureFlags)
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Success::class.java)
            }
        }
    }

    @Test
    fun `present - ensures Room directory search is not present on release Google Play builds`() = runTest {
        val buildMeta = aBuildMeta(buildType = BuildType.RELEASE, flavorDescription = "GooglePlay")
        val presenter = createDeveloperSettingsPresenter(buildMeta = buildMeta)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.features).doesNotContain(FeatureFlags.RoomDirectorySearch)
            }
        }
    }

    @Test
    fun `present - ensures state is updated when enabled feature event is triggered`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                val feature = state.features.first()
                state.eventSink(DeveloperSettingsEvents.UpdateEnabledFeature(feature, !feature.isEnabled))
            }
            awaitItem().also { state ->
                val feature = state.features.first()
                assertThat(feature.isEnabled).isTrue()
                assertThat(feature.key).isEqualTo(feature.key)
            }
        }
    }

    @Test
    fun `present - clear cache`() = runTest {
        val clearCacheUseCase = FakeClearCacheUseCase()
        val presenter = createDeveloperSettingsPresenter(clearCacheUseCase = clearCacheUseCase)
        presenter.test {
            skipItems(2)
            assertThat(clearCacheUseCase.executeHasBeenCalled).isFalse()
            awaitItem().also { state ->
                state.eventSink(DeveloperSettingsEvents.ClearCache)
            }
            awaitItem().also { state ->
                assertThat(state.clearCacheAction).isInstanceOf(AsyncAction.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.clearCacheAction).isInstanceOf(AsyncAction.Success::class.java)
                assertThat(clearCacheUseCase.executeHasBeenCalled).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Loading::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Success::class.java)
            }
        }
    }

    @Test
    fun `present - custom element call base url`() = runTest {
        val preferencesStore = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferencesStore)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.customElementCallBaseUrlState.baseUrl).isNull()
                state.eventSink(DeveloperSettingsEvents.SetCustomElementCallBaseUrl("https://call.element.ahoy"))
            }
            awaitItem().also { state ->
                assertThat(state.customElementCallBaseUrlState.baseUrl).isEqualTo("https://call.element.ahoy")
                assertThat(state.customElementCallBaseUrlState.defaultUrl).isEqualTo(ElementCallConfig.DEFAULT_BASE_URL)
            }
        }
    }

    @Test
    fun `present - custom element call base url validator needs at least an HTTP scheme and host`() = runTest {
        val presenter = createDeveloperSettingsPresenter()
        presenter.test {
            skipItems(2)
            val urlValidator = awaitItem().customElementCallBaseUrlState.validator
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
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.isSimpleSlidingSyncEnabled).isFalse()
                state.eventSink(DeveloperSettingsEvents.SetSimplifiedSlidingSyncEnabled(true))
            }
            awaitItem().also { state ->
                assertThat(state.isSimpleSlidingSyncEnabled).isTrue()
                assertThat(preferences.isSimplifiedSlidingSyncEnabledFlow().first()).isTrue()
                advanceUntilIdle()
                logoutCallRecorder.assertions().isCalledOnce()
                state.eventSink(DeveloperSettingsEvents.SetSimplifiedSlidingSyncEnabled(false))
            }
            awaitItem().also { state ->
                assertThat(state.isSimpleSlidingSyncEnabled).isFalse()
                assertThat(preferences.isSimplifiedSlidingSyncEnabledFlow().first()).isFalse()
                advanceUntilIdle()
                logoutCallRecorder.assertions().isCalledExactly(2)
            }
        }
    }

    @Test
    fun `present - toggling hide image and video`() = runTest {
        val preferences = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferences)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.hideImagesAndVideos).isFalse()
                state.eventSink(DeveloperSettingsEvents.SetHideImagesAndVideos(true))
            }
            awaitItem().also { state ->
                assertThat(state.hideImagesAndVideos).isTrue()
                assertThat(preferences.doesHideImagesAndVideosFlow().first()).isTrue()
                state.eventSink(DeveloperSettingsEvents.SetHideImagesAndVideos(false))
            }
            awaitItem().also { state ->
                assertThat(state.hideImagesAndVideos).isFalse()
                assertThat(preferences.doesHideImagesAndVideosFlow().first()).isFalse()
            }
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
