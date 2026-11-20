/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.location

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.preferences.impl.R
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class LocationSettingsViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LocationSettingsEvent>(expectEvents = false)
        ensureCalledOnce {
            setLocationSettingsView(
                state = aLocationSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `the current minimum distance is displayed`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LocationSettingsEvent>(expectEvents = false)
        setLocationSettingsView(
            state = aLocationSettingsState(
                liveLocationMinimumDistanceUpdate = 50,
                eventSink = eventsRecorder,
            ),
        )
        val text = activity!!.resources.getQuantityString(
            R.plurals.screen_advanced_settings_live_location_update_distance,
            50,
            50,
        )
        onNodeWithText(text).assertExists()
    }

    @Test
    fun `changing the minimum distance emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LocationSettingsEvent>()
        setLocationSettingsView(
            state = aLocationSettingsState(
                liveLocationMinimumDistanceUpdate = 50,
                eventSink = eventsRecorder,
            ),
        )
        // `setProgress` invokes `onValueChange` and then `onValueChangeFinish`, which is what the view listens to.
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(42f) }
        eventsRecorder.assertSingle(LocationSettingsEvent.SetLiveLocationMinimumDistanceUpdate(42))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLocationSettingsView(
    state: LocationSettingsState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onOpenAppSettingsClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        LocationSettingsView(
            state = state,
            onBackClick = onBackClick,
            onOpenAppSettingsClick = onOpenAppSettingsClick,
        )
    }
}
