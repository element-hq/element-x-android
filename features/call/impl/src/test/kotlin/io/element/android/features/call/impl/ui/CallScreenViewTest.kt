/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        val pipEventsRecorder = EventsRecorder<PictureInPictureEvents>(expectEvents = false)
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
        pipEventsRecorder.assertSingle(PictureInPictureEvents.EnterPictureInPicture)
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
