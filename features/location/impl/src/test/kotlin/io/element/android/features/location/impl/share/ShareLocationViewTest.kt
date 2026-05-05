/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.location.impl.share

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareLocationViewTest {
    @Test
    fun `test back action`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setShareLocationView(
                state = aShareLocationState(
                    eventSink = eventsRecorder
                ),
                navigateUp = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `test fab click`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        onNodeWithTag(TestTags.floatingActionButton.value).performClick()
        eventsRecorder.assertSingle(ShareLocationEvent.StartTrackingUserLocation)
    }

    @Test
    fun `when permission denied is displayed user can open the settings`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.OpenAppSettings)
    }

    @Test
    fun `when permission denied is displayed user can close the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }

    @Test
    fun `when permission rationale is displayed user can request permissions`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.RequestPermissions)
    }

    @Test
    fun `when permission rationale is displayed user can close the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }

    @Test
    fun `when location service disabled is displayed user can open location settings`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                hasLocationPermission = true,
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShareLocationEvent.OpenLocationSettings)
    }

    @Test
    fun `when location service disabled is displayed user can close the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShareLocationEvent>()
        setShareLocationView(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                hasLocationPermission = true,
                eventSink = eventsRecorder
            ),
            navigateUp = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShareLocationEvent.DismissDialog)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setShareLocationView(
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
