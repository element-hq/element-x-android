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

package io.element.android.features.preferences.impl.developer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class DeveloperSettingsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setDeveloperSettingsView(
                state = aDeveloperSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on element call url open the dialogs and submit emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>()
        rule.setDeveloperSettingsView(
            state = aDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_element_call_base_url)
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(DeveloperSettingsEvents.SetCustomElementCallBaseUrl("https://call.element.io"))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on open showkase invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setDeveloperSettingsView(
                state = aDeveloperSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenShowkase = it
            )
            rule.onNodeWithText("Open Showkase browser").performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on configure tracing invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setDeveloperSettingsView(
                state = aDeveloperSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenConfigureTracing = it
            )
            rule.onNodeWithText("Configure tracing").performClick()
        }
    }

    @Config(qualifiers = "h1500dp")
    @Test
    fun `clicking on clear cache emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>()
        rule.setDeveloperSettingsView(
            state = aDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Clear cache").performClick()
        eventsRecorder.assertSingle(DeveloperSettingsEvents.ClearCache)
    }

    @Config(qualifiers = "h1500dp")
    @Test
    fun `clicking on the simplified sliding sync switch emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>()
        rule.setDeveloperSettingsView(
            state = aDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Enable Simplified Sliding Sync").performClick()
        eventsRecorder.assertSingle(DeveloperSettingsEvents.SetSimplifiedSlidingSyncEnabled(true))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setDeveloperSettingsView(
    state: DeveloperSettingsState,
    onOpenShowkase: () -> Unit = EnsureNeverCalled(),
    onOpenConfigureTracing: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled()
) {
    setContent {
        DeveloperSettingsView(
            state = state,
            onOpenShowkase = onOpenShowkase,
            onOpenConfigureTracing = onOpenConfigureTracing,
            onBackClick = onBackClick,
        )
    }
}
