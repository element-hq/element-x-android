/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.theme.Theme
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
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
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isDeveloperModeEnabled).isFalse()
            assertThat(initialState.showChangeThemeDialog).isFalse()
            assertThat(initialState.isSharePresenceEnabled).isTrue()
            assertThat(initialState.theme).isEqualTo(Theme.System)
        }
    }

    @Test
    fun `present - developer mode on off`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
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
    fun `present - share presence off on`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.isSharePresenceEnabled).isTrue()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetSharePresenceEnabled(false))
            assertThat(awaitItem().isSharePresenceEnabled).isFalse()
            initialState.eventSink.invoke(AdvancedSettingsEvents.SetSharePresenceEnabled(true))
            assertThat(awaitItem().isSharePresenceEnabled).isTrue()
        }
    }

    @Test
    fun `present - change theme`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitLastSequentialItem()
            initialState.eventSink.invoke(AdvancedSettingsEvents.ChangeTheme)
            val withDialog = awaitItem()
            assertThat(withDialog.showChangeThemeDialog).isTrue()
            // Cancel
            withDialog.eventSink(AdvancedSettingsEvents.CancelChangeTheme)
            val withoutDialog = awaitItem()
            assertThat(withoutDialog.showChangeThemeDialog).isFalse()
            withDialog.eventSink.invoke(AdvancedSettingsEvents.ChangeTheme)
            assertThat(awaitItem().showChangeThemeDialog).isTrue()
            withDialog.eventSink(AdvancedSettingsEvents.SetTheme(Theme.Light))
            val withNewTheme = awaitItem()
            assertThat(withNewTheme.showChangeThemeDialog).isFalse()
            assertThat(withNewTheme.theme).isEqualTo(Theme.Light)
        }
    }

    private fun createAdvancedSettingsPresenter(
        appPreferencesStore: InMemoryAppPreferencesStore = InMemoryAppPreferencesStore(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
    ) = AdvancedSettingsPresenter(
        appPreferencesStore = appPreferencesStore,
        sessionPreferencesStore = sessionPreferencesStore,
    )
}
