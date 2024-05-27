/*
 * Copyright (c) 2024 New Vector Ltd
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
                onBackPressed = it
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
    onBackPressed: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AdvancedSettingsView(
            state = state,
            onBackPressed = onBackPressed,
        )
    }
}
