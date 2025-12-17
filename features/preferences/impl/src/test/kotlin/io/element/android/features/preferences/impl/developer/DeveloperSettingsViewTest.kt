/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.developer.tracing.LogLevelItem
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

    @Config(qualifiers = "h1500dp")
    @Test
    fun `clicking on push history notification invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setDeveloperSettingsView(
                state = aDeveloperSettingsState(
                    eventSink = eventsRecorder
                ),
                onPushHistoryClick = it
            )
            rule.clickOn(R.string.troubleshoot_notifications_entry_point_push_history_title)
        }
    }

    @Config(qualifiers = "h1500dp")
    @Test
    fun `clicking on element call url open the dialogs and submit emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>()
        rule.setDeveloperSettingsView(
            state = aDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_element_call_base_url)
        val textInputNode = rule.onAllNodes(isEditable().and(isFocusable())).filterToOne(hasAnyAncestor(isDialog()))
        textInputNode.performTextInput("https://call.element.dev")
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(DeveloperSettingsEvents.SetCustomElementCallBaseUrl("https://call.element.dev"))
    }

    @Config(qualifiers = "h2000dp")
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
    fun `clicking on log level emits the expected event`() {
        val eventsRecorder = EventsRecorder<DeveloperSettingsEvents>()
        rule.setDeveloperSettingsView(
            state = aDeveloperSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.onNodeWithText("Tracing log level").performClick()
        rule.onNodeWithText("Debug").performClick()
        eventsRecorder.assertSingle(DeveloperSettingsEvents.SetTracingLogLevel(LogLevelItem.DEBUG))
    }

    @Config(qualifiers = "h2000dp")
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
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setDeveloperSettingsView(
    state: DeveloperSettingsState,
    onOpenShowkase: () -> Unit = EnsureNeverCalled(),
    onPushHistoryClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled()
) {
    setContent {
        DeveloperSettingsView(
            state = state,
            onOpenShowkase = onOpenShowkase,
            onPushHistoryClick = onPushHistoryClick,
            onBackClick = onBackClick,
        )
    }
}
