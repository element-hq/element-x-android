/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
