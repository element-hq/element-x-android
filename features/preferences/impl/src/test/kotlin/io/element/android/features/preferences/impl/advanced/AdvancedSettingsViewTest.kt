/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.compound.theme.Theme
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdvancedSettingsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setAdvancedSettingsView(
                state = aAdvancedSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on Appearance emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.common_appearance)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.ChangeTheme)
    }

    @Test
    fun `clicking on other theme emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
                showChangeThemeDialog = true
            ),
        )
        rule.clickOn(CommonStrings.common_dark)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetTheme(Theme.Dark))
    }

    @Test
    fun `clicking on View source emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_view_source)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetDeveloperModeEnabled(true))
    }

    @Test
    fun `clicking on Share presence emits the expected event`() {
        val eventsRecorder = EventsRecorder<AdvancedSettingsEvents>()
        rule.setAdvancedSettingsView(
            state = aAdvancedSettingsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_share_presence)
        eventsRecorder.assertSingle(AdvancedSettingsEvents.SetSharePresenceEnabled(true))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setAdvancedSettingsView(
    state: AdvancedSettingsState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AdvancedSettingsView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
