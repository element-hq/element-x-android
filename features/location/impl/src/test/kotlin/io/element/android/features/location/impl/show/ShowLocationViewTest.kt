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

package io.element.android.features.location.impl.show

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.testtags.TestTags
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
class ShowLocationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test back action`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setShowLocationView(
                state = aShowLocationState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `test share action`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        val shareContentDescription = rule.activity.getString(CommonStrings.action_share)
        rule.onNodeWithContentDescription(shareContentDescription).performClick()
        eventsRecorder.assertSingle(ShowLocationEvents.Share)
    }

    @Test
    fun `test fab click`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        rule.onNodeWithTag(TestTags.floatingActionButton.value).performClick()
        eventsRecorder.assertSingle(ShowLocationEvents.TrackMyLocation(true))
    }

    @Test
    fun `when permission denied is displayed user can open the settings`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionDenied,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShowLocationEvents.OpenAppSettings)
    }

    @Test
    fun `when permission denied is displayed user can close the dialog`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionDenied,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShowLocationEvents.DismissDialog)
    }

    @Test
    fun `when permission rationale is displayed user can request permissions`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionRationale,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShowLocationEvents.RequestPermissions)
    }

    @Test
    fun `when permission rationale is displayed user can close the dialog`() {
        val eventsRecorder = EventsRecorder<ShowLocationEvents>()
        rule.setShowLocationView(
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionRationale,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShowLocationEvents.DismissDialog)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setShowLocationView(
    state: ShowLocationState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        // Simulate a LocalInspectionMode for MapLibreMap
        CompositionLocalProvider(LocalInspectionMode provides true) {
            ShowLocationView(
                state = state,
                onBackClick = onBackClick,
            )
        }
    }
}
