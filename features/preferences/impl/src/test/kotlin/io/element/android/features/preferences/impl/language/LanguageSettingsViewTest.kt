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

package io.element.android.features.preferences.impl.language

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LanguageSettingsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<LanguageSettingsEvents>()
        ensureCalledOnce {
            rule.setLanguageSettingsView(
                state = aLanguageSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackPressed = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on a language invokes the expected callback`() {
        val locale = Locale.forLanguageTag("de")
        val eventsRecorder = EventsRecorder<LanguageSettingsEvents>()
        rule.setLanguageSettingsView(
            state = aLanguageSettingsState(
                eventSink = eventsRecorder
            )
        )
        rule.onNodeWithText(locale.displayName).performClick()
        eventsRecorder.assertSingle(LanguageSettingsEvents.SetLocale(locale))
    }

    @Test
    fun `clicking on system default invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<LanguageSettingsEvents>()
        rule.setLanguageSettingsView(
            state = aLanguageSettingsState(
                eventSink = eventsRecorder
            )
        )
        rule.clickOn(R.string.screen_language_settings_system_default)
        eventsRecorder.assertSingle(LanguageSettingsEvents.SetToDefault)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setLanguageSettingsView(
        state: LanguageSettingsState,
        onBackPressed: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            LanguageSettingsView(
                state = state,
                onBackPressed = onBackPressed,
            )
        }
    }
}
