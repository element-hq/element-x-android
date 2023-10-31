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

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.featureflag.test.InMemoryPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AdvancedSettingsPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService()
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isDeveloperModeEnabled).isFalse()
            assertThat(initialState.isRichTextEditorEnabled).isFalse()
            assertThat(initialState.customElementCallBaseUrlState?.baseUrl).isNull()
        }
    }

    @Test
    fun `present - developer mode on off`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService()
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isDeveloperModeEnabled).isFalse()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetDeveloperModeEnabled(true))
            assertThat(awaitItem().isDeveloperModeEnabled).isTrue()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetDeveloperModeEnabled(false))
            assertThat(awaitItem().isDeveloperModeEnabled).isFalse()
        }
    }

    @Test
    fun `present - rich text editor on off`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService()
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isRichTextEditorEnabled).isFalse()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetRichTextEditorEnabled(true))
            assertThat(awaitItem().isRichTextEditorEnabled).isTrue()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetRichTextEditorEnabled(false))
            assertThat(awaitItem().isRichTextEditorEnabled).isFalse()
        }
    }

    @Test
    fun `present - custom element call url state is null if the feature flag is disabled`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService().apply {
            setFeatureEnabled(FeatureFlags.InRoomCalls, false)
        }
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.customElementCallBaseUrlState).isNull()
        }
    }

    @Test
    fun `present - custom element call base url`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService(initialState = hashMapOf(FeatureFlags.InRoomCalls.key to true))
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.customElementCallBaseUrlState).isNotNull()
            assertThat(initialState.customElementCallBaseUrlState?.baseUrl).isNull()

            initialState.eventSink(AdvancedSettingsEvents.SetCustomElementCallBaseUrl("https://call.element.ahoy"))
            val updatedItem = awaitItem()
            assertThat(updatedItem.customElementCallBaseUrlState?.baseUrl).isEqualTo("https://call.element.ahoy")
        }
    }

    @Test
    fun `present - custom element call base url validator needs at least an HTTP scheme and host`() = runTest {
        val store = InMemoryPreferencesStore()
        val featureFlagService = FakeFeatureFlagService().apply {
            setFeatureEnabled(FeatureFlags.InRoomCalls, true)
        }
        val presenter = AdvancedSettingsPresenter(store, featureFlagService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val urlValidator = awaitLastSequentialItem().customElementCallBaseUrlState!!.validator
            assertThat(urlValidator("")).isTrue() // We allow empty string to clear the value and use the default one
            assertThat(urlValidator("test")).isFalse()
            assertThat(urlValidator("http://")).isFalse()
            assertThat(urlValidator("geo://test")).isFalse()
            assertThat(urlValidator("https://call.element.io")).isTrue()
        }
    }
}
