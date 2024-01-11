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

package io.element.android.features.preferences.impl.developer

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.preferences.impl.tasks.FakeClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.FakeComputeCacheSizeUseCase
import io.element.android.features.rageshake.impl.preferences.DefaultRageshakePreferencesPresenter
import io.element.android.features.rageshake.test.rageshake.FakeRageShake
import io.element.android.features.rageshake.test.rageshake.FakeRageshakeDataStore
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.featureflag.test.InMemoryPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
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
            val loadedState = awaitItem()
            assertThat(loadedState.rageshakeState.isEnabled).isFalse()
            assertThat(loadedState.rageshakeState.isSupported).isTrue()
            assertThat(loadedState.rageshakeState.sensitivity).isEqualTo(1.0f)
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
        val rageshakePresenter = DefaultRageshakePreferencesPresenter(FakeRageShake(), FakeRageshakeDataStore())
        val clearCacheUseCase = FakeClearCacheUseCase()
        val presenter = createDeveloperSettingsPresenter(clearCacheUseCase = clearCacheUseCase, rageshakePresenter = rageshakePresenter)
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
        val preferencesStore = InMemoryPreferencesStore()
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

    private fun createDeveloperSettingsPresenter(
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
        cacheSizeUseCase: FakeComputeCacheSizeUseCase = FakeComputeCacheSizeUseCase(),
        clearCacheUseCase: FakeClearCacheUseCase = FakeClearCacheUseCase(),
        rageshakePresenter: DefaultRageshakePreferencesPresenter = DefaultRageshakePreferencesPresenter(FakeRageShake(), FakeRageshakeDataStore()),
        preferencesStore: InMemoryPreferencesStore = InMemoryPreferencesStore(),
    ): DeveloperSettingsPresenter {
        return DeveloperSettingsPresenter(
            featureFlagService = featureFlagService,
            computeCacheSizeUseCase = cacheSizeUseCase,
            clearCacheUseCase = clearCacheUseCase,
            rageshakePresenter = rageshakePresenter,
            preferencesStore = preferencesStore,
        )
    }
}
