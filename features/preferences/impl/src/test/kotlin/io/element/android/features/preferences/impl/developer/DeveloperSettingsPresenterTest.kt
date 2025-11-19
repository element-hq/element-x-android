/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.developer

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.preferences.impl.tasks.FakeClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.FakeComputeCacheSizeUseCase
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeveloperSettingsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - ensures initial states are correct`() = runTest {
        val getAvailableFeaturesResult = lambdaRecorder<Boolean, Boolean, List<Feature>> { _, _ ->
            listOf(
                FakeFeature(
                    key = "feature_1",
                    title = "Feature 1",
                    isInLabs = false,
                )
            )
        }
        val presenter = createDeveloperSettingsPresenter(
            featureFlagService = FakeFeatureFlagService(getAvailableFeaturesResult = getAvailableFeaturesResult)
        )
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.features).isEmpty()
                assertThat(state.clearCacheAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(state.cacheSize).isEqualTo(AsyncData.Uninitialized)
                assertThat(state.customElementCallBaseUrlState).isNotNull()
                assertThat(state.customElementCallBaseUrlState.baseUrl).isNull()
                assertThat(state.rageshakeState.isEnabled).isFalse()
                assertThat(state.rageshakeState.isSupported).isTrue()
                assertThat(state.rageshakeState.sensitivity).isEqualTo(0.3f)
                assertThat(state.tracingLogLevel).isEqualTo(AsyncData.Uninitialized)
                assertThat(state.isEnterpriseBuild).isFalse()
                assertThat(state.showColorPicker).isFalse()
            }
            awaitItem().also { state ->
                assertThat(state.features).isNotEmpty()
                assertThat(state.features).hasSize(1)
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.INFO)
            }
            awaitItem().also { state ->
                assertThat(state.cacheSize).isInstanceOf(AsyncData.Success::class.java)
            }
            getAvailableFeaturesResult.assertions().isCalledOnce()
                .with(value(false), value(false))
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
                val feature = state.features.first { !it.isEnabled }
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
    fun `present - changing tracing log level`() = runTest {
        val preferences = InMemoryAppPreferencesStore()
        val presenter = createDeveloperSettingsPresenter(preferencesStore = preferences)
        presenter.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.INFO)
                state.eventSink(DeveloperSettingsEvents.SetTracingLogLevel(LogLevelItem.TRACE))
            }
            awaitItem().also { state ->
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.TRACE)
            }
        }
    }

    @Test
    fun `present - enterprise build can change the brand color`() = runTest {
        val overrideBrandColorResult = lambdaRecorder<SessionId?, String?, Unit> { _, _ -> }
        val presenter = createDeveloperSettingsPresenter(
            enterpriseService = FakeEnterpriseService(
                isEnterpriseBuild = true,
                overrideBrandColorResult = overrideBrandColorResult,
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnterpriseBuild).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(false))
            assertThat(awaitItem().showColorPicker).isFalse()
            initialState.eventSink(DeveloperSettingsEvents.SetShowColorPicker(true))
            assertThat(awaitItem().showColorPicker).isTrue()
            initialState.eventSink(DeveloperSettingsEvents.ChangeBrandColor(Color.Green))
            assertThat(awaitItem().showColorPicker).isFalse()
            skipItems(1)
            overrideBrandColorResult.assertions().isCalledOnce()
                .with(value(A_SESSION_ID), value("#00FF00"))
        }
    }

    private fun createDeveloperSettingsPresenter(
        sessionId: SessionId = A_SESSION_ID,
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(
            getAvailableFeaturesResult = { _, _ ->
                listOf(
                    FakeFeature(
                        key = "feature_1",
                        title = "Feature 1",
                        isInLabs = false,
                    )
                )
            }
        ),
        cacheSizeUseCase: FakeComputeCacheSizeUseCase = FakeComputeCacheSizeUseCase(),
        clearCacheUseCase: FakeClearCacheUseCase = FakeClearCacheUseCase(),
        preferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        buildMeta: BuildMeta = aBuildMeta(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
    ): DeveloperSettingsPresenter {
        return DeveloperSettingsPresenter(
            sessionId = sessionId,
            featureFlagService = featureFlagService,
            computeCacheSizeUseCase = cacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
            rageshakePresenter = { aRageshakePreferencesState() },
            appPreferencesStore = preferencesStore,
            buildMeta = buildMeta,
            enterpriseService = enterpriseService,
        )
    }
}
