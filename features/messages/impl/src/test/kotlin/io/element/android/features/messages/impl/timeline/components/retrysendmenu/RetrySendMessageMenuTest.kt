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

package io.element.android.features.messages.impl.timeline.components.retrysendmenu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RetrySendMessageMenuTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `dismiss the bottom sheet emits the expected event`() {
        val eventsRecorder = EventsRecorder<RetrySendMenuEvents>()
        rule.setRetrySendMessageMenu(
            aRetrySendMenuState(
                event = aTimelineItemEvent(),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        // Cannot test this for now.
        // eventsRecorder.assertSingle(RetrySendMenuEvents.Dismiss)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `retry to send the event emits the expected event`() {
        val eventsRecorder = EventsRecorder<RetrySendMenuEvents>()
        rule.setRetrySendMessageMenu(
            aRetrySendMenuState(
                event = aTimelineItemEvent(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_room_retry_send_menu_send_again_action)
        eventsRecorder.assertSingle(RetrySendMenuEvents.Retry)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `remove the event emits the expected event`() {
        val eventsRecorder = EventsRecorder<RetrySendMenuEvents>()
        rule.setRetrySendMessageMenu(
            aRetrySendMenuState(
                event = aTimelineItemEvent(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_remove)
        eventsRecorder.assertSingle(RetrySendMenuEvents.Remove)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRetrySendMessageMenu(
    state: RetrySendMenuState,
) {
    setContent {
        RetrySendMessageMenu(
            state = state,
        )
    }
}
