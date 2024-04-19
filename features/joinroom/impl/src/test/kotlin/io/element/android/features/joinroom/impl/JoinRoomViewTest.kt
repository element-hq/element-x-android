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

package io.element.android.features.joinroom.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
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
class JoinRoomViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke the expected callback`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setJoinRoomView(
                aJoinRoomState(
                    eventSink = eventsRecorder,
                ),
                onBackPressed = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on Join room on CanJoin room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_join_action)
        eventsRecorder.assertSingle(JoinRoomEvents.JoinRoom)
    }

    @Test
    fun `clicking on Knock room on CanKnock room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_join_room_knock_action)
        eventsRecorder.assertSingle(JoinRoomEvents.KnockRoom)
    }

    @Test
    fun `clicking on closing Knock error emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock),
                knockAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(JoinRoomEvents.ClearError)
    }

    @Test
    fun `clicking on Accept invitation IsInvited room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(null)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_accept)
        eventsRecorder.assertSingle(JoinRoomEvents.AcceptInvite)
    }

    @Test
    fun `clicking on Decline invitation on IsInvited room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(null)),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertSingle(JoinRoomEvents.DeclineInvite)
    }

    @Test
    fun `clicking on Retry when an error occurs emits the expected Event`() {
        val eventsRecorder = EventsRecorder<JoinRoomEvents>()
        rule.setJoinRoomView(
            aJoinRoomState(
                contentState = aFailureContentState(),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(JoinRoomEvents.RetryFetchingContent)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setJoinRoomView(
    state: JoinRoomState,
    onBackPressed: () -> Unit = EnsureNeverCalled(),
    onKnockSuccess: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        JoinRoomView(
            state = state,
            onBackPressed = onBackPressed,
            onKnockSuccess = onKnockSuccess,
        )
    }
}
