/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
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
class ShareLocationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test back action`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setShareLocationView(
                state = aShareLocationState(
                    eventSink = eventsRecorder
                ),
                navigateUp = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `test fab click`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.onNodeWithTag(TestTags.floatingActionButton.value).performClick()
        eventsRecorder.assertSingle(ShareLocationEvent.StartTrackingUserLocation)
    }

    @Test
    fun `when permission denied is displayed user can open the settings`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.OpenAppSettings)
    }

    @Test
    fun `when permission denied is displayed user can close the dialog`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }

    @Test
    fun `when permission rationale is displayed user can request permissions`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.RequestPermissions)
    }

    @Test
    fun `when permission rationale is displayed user can close the dialog`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }

    @Test
    fun `when location service disabled is displayed user can open location settings`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                hasLocationPermission = true,
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.OpenLocationSettings)
    }

    @Test
    fun `when location service disabled is displayed user can close the dialog`() {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        rule.setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                hasLocationPermission = true,
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setShareLocationView(
    state: ShareLocationState,
    navigateUp: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        // Simulate a LocalInspectionMode for MapLibreMap
        CompositionLocalProvider(LocalInspectionMode provides true) {
            ShareLocationView(
                state = state,
                navigateUp = navigateUp,
            )
        }
    }
}
