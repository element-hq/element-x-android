/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.call.impl.pip.PictureInPictureEvents
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back when pip is not supported hangs up`() {
        val eventsRecorder = EventsRecorder<CallScreenEvents>()
        val pipEventsRecorder = EventsRecorder<PictureInPictureEvents>()
        rule.setCallScreenView(
            aCallScreenState(
                eventSink = eventsRecorder
            ),
            aPictureInPictureState(
                supportPip = false,
                eventSink = pipEventsRecorder,
            ),
        )
        rule.pressBack()
        eventsRecorder.assertSize(2)
        eventsRecorder.assertTrue(0) { it is CallScreenEvents.SetupMessageChannels }
        eventsRecorder.assertTrue(1) { it == CallScreenEvents.Hangup }
        pipEventsRecorder.assertSize(1)
        pipEventsRecorder.assertTrue(0) { it is PictureInPictureEvents.SetPipController }
    }

    @Test
    fun `clicking on back when pip is supported enables PiP`() {
        val eventsRecorder = EventsRecorder<CallScreenEvents>()
        val pipEventsRecorder = EventsRecorder<PictureInPictureEvents>()
        rule.setCallScreenView(
            aCallScreenState(
                eventSink = eventsRecorder
            ),
            aPictureInPictureState(
                supportPip = true,
                eventSink = pipEventsRecorder,
            ),
        )
        rule.pressBack()
        eventsRecorder.assertSize(1)
        eventsRecorder.assertTrue(0) { it is CallScreenEvents.SetupMessageChannels }
        pipEventsRecorder.assertSize(2)
        pipEventsRecorder.assertTrue(0) { it is PictureInPictureEvents.SetPipController }
        pipEventsRecorder.assertTrue(1) { it == PictureInPictureEvents.EnterPictureInPicture }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setCallScreenView(
    state: CallScreenState,
    pipState: PictureInPictureState,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit = { _, _ -> },
) {
    setContent {
        CallScreenView(
            state = state,
            pipState = pipState,
            requestPermissions = requestPermissions,
        )
    }
}
