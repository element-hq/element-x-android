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

package io.element.android.features.verifysession.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class VerifySelfSessionViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `back key pressed - when canceled resets the flow`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Canceled,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.Reset)
    }

    @Test
    fun `back key pressed - when awaiting response cancels the verification`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.AwaitingOtherDeviceResponse,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when ready to verify cancels the verification`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Ready,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when verifying and not loading declines the verification`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.DeclineVerification)
    }

    @Test
    fun `back key pressed - when verifying and loading does nothing`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Loading(),
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `when flow is completed and the user clicks on the continue button, the expected callback is invoked`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Completed,
                    eventSink = eventsRecorder
                ),
                onFinished = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enter recovery key calls the expected callback`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial(true, false),
                    eventSink = eventsRecorder
                ),
                onEnterRecoveryKey = callback,
            )
            rule.clickOn(R.string.screen_session_verification_enter_recovery_key)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on create new recovery key calls the expected callback`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial(true, true),
                    eventSink = eventsRecorder
                ),
                onCreateNewRecoveryKey = callback,
            )
            rule.clickOn(R.string.screen_identity_confirmation_create_new_recovery_key)
        }
    }

    @Test
    fun `clicking on they match emits the expected event`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_match)
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.ConfirmVerification)
    }

    @Test
    fun `clicking on they do not match emits the expected event`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_dont_match)
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.DeclineVerification)
    }

    private fun <R: TestRule> AndroidComposeTestRule<R, ComponentActivity>.setVerifySelfSessionView(
        state: VerifySelfSessionState,
        onEnterRecoveryKey: () -> Unit = EnsureNeverCalled(),
        onCreateNewRecoveryKey: () -> Unit  = EnsureNeverCalled(),
        onFinished: () -> Unit  = EnsureNeverCalled(),
    ) {
        rule.setContent {
            VerifySelfSessionView(
                state = state,
                onEnterRecoveryKey = onEnterRecoveryKey,
                onCreateNewRecoveryKey = onCreateNewRecoveryKey,
                onFinished = onFinished,
            )
        }
    }
}
