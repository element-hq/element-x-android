/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.preferences.impl.developer.appsettings

import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
import io.element.android.features.rageshake.api.preferences.aRageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
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

class AppDeveloperSettingsPresenterTest {
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
        val presenter = createAppDeveloperSettingsPresenter(
            featureFlagService = FakeFeatureFlagService(getAvailableFeaturesResult = getAvailableFeaturesResult),
        )
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.features).isEmpty()
                assertThat(state.customElementCallBaseUrlState).isNotNull()
                assertThat(state.customElementCallBaseUrlState.baseUrl).isNull()
                assertThat(state.rageshakeState.isEnabled).isFalse()
                assertThat(state.rageshakeState.isSupported).isTrue()
                assertThat(state.rageshakeState.sensitivity).isEqualTo(0.3f)
                assertThat(state.tracingLogLevel).isEqualTo(AsyncData.Uninitialized)
            }
            awaitItem().also { state ->
                assertThat(state.features).isNotEmpty()
                assertThat(state.features).hasSize(1)
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.INFO)
            }
            getAvailableFeaturesResult.assertions().isCalledOnce()
                .with(value(false), value(false))
        }
    }

    @Test
    fun `present - ensures Room directory search is not present on release Google Play builds`() = runTest {
        val buildMeta = aBuildMeta(buildType = BuildType.RELEASE, flavorDescription = "GooglePlay")
        val presenter = createAppDeveloperSettingsPresenter(buildMeta = buildMeta)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.features).doesNotContain(FeatureFlags.RoomDirectorySearch)
            }
        }
    }

    @Test
    fun `present - ensures state is updated when enabled feature event is triggered`() = runTest {
        val presenter = createAppDeveloperSettingsPresenter()
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                val feature = state.features.first { !it.isEnabled }
                state.eventSink(AppDeveloperSettingsEvent.UpdateEnabledFeature(feature, !feature.isEnabled))
            }
            awaitItem().also { state ->
                val feature = state.features.first()
                assertThat(feature.isEnabled).isTrue()
                assertThat(feature.key).isEqualTo(feature.key)
            }
        }
    }

    @Test
    fun `present - custom element call base url`() = runTest {
        val preferencesStore = InMemoryAppPreferencesStore()
        val presenter = createAppDeveloperSettingsPresenter(preferencesStore = preferencesStore)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.customElementCallBaseUrlState.baseUrl).isNull()
                state.eventSink(AppDeveloperSettingsEvent.SetCustomElementCallBaseUrl("https://call.element.ahoy"))
            }
            awaitItem().also { state ->
                assertThat(state.customElementCallBaseUrlState.baseUrl).isEqualTo("https://call.element.ahoy")
            }
        }
    }

    @Test
    fun `present - custom element call base url validator needs at least an HTTP scheme and host`() = runTest {
        val presenter = createAppDeveloperSettingsPresenter()
        presenter.test {
            skipItems(1)
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
        val presenter = createAppDeveloperSettingsPresenter(preferencesStore = preferences)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.INFO)
                state.eventSink(AppDeveloperSettingsEvent.SetTracingLogLevel(LogLevelItem.TRACE))
            }
            awaitItem().also { state ->
                assertThat(state.tracingLogLevel.dataOrNull()).isEqualTo(LogLevelItem.TRACE)
            }
        }
    }

    private fun createAppDeveloperSettingsPresenter(
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
        preferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        buildMeta: BuildMeta = aBuildMeta(),
    ): AppDeveloperSettingsPresenter {
        return AppDeveloperSettingsPresenter(
            featureFlagService = featureFlagService,
            rageshakePresenter = { aRageshakePreferencesState() },
            appPreferencesStore = preferencesStore,
            buildMeta = buildMeta,
        )
    }
}
