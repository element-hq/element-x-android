/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
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
                assertThat(doesCompressMedia).isTrue()
                assertThat(theme).isEqualTo(ThemeOption.System)
            }
        }
    }

    @Test
    fun `present - developer mode on off`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
            with(awaitItem()) {
                assertThat(doesCompressMedia).isTrue()
                eventSink(AdvancedSettingsEvents.SetCompressMedia(false))
            }
            with(awaitItem()) {
                assertThat(doesCompressMedia).isFalse()
                eventSink(AdvancedSettingsEvents.SetCompressMedia(true))
            }
            with(awaitItem()) {
                assertThat(doesCompressMedia).isTrue()
            }
        }
    }

    @Test
    fun `present - change theme`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(hideInviteAvatars).isFalse()
                eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(true))
            }
            with(awaitItem()) {
                assertThat(hideInviteAvatars).isTrue()
                eventSink(AdvancedSettingsEvents.SetHideInviteAvatars(false))
            }
            with(awaitItem()) {
                assertThat(hideInviteAvatars).isFalse()
            }
        }
    }

    @Test
    fun `present - timeline media preview value`() = runTest {
        val presenter = createAdvancedSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            with(awaitItem()) {
                assertThat(timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.On)
                eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
            }
            with(awaitItem()) {
                assertThat(timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Off)
                eventSink(AdvancedSettingsEvents.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
            }
            with(awaitItem()) {
                assertThat(timelineMediaPreviewValue).isEqualTo(MediaPreviewValue.Private)
            }
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
