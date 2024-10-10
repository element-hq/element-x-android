/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
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
    fun `back key pressed - on Completed step does nothing`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Completed,
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
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial(true),
                    eventSink = eventsRecorder
                ),
                onEnterRecoveryKey = callback,
            )
            rule.clickOn(R.string.screen_session_verification_enter_recovery_key)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on learn more invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial(true),
                    eventSink = eventsRecorder
                ),
                onLearnMoreClick = callback,
            )
            rule.clickOn(CommonStrings.action_learn_more)
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

    @Test
    fun `clicking on 'Skip' emits the expected event`() {
        val eventsRecorder = EventsRecorder<VerifySelfSessionViewEvents>()
        rule.setVerifySelfSessionView(
            aVerifySelfSessionState(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial(canEnterRecoveryKey = true),
                displaySkipButton = true,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_skip)
        eventsRecorder.assertSingle(VerifySelfSessionViewEvents.SkipVerification)
    }

    @Test
    fun `on Skipped step - onFinished callback is called immediately`() {
        ensureCalledOnce { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    verificationFlowStep = VerifySelfSessionState.VerificationStep.Skipped,
                    displaySkipButton = true,
                    eventSink = EnsureNeverCalledWithParam(),
                ),
                onFinished = callback,
            )
        }
    }

    @Test
    fun `on success logout - onFinished callback is called immediately`() {
        val aUrl = "aUrl"
        ensureCalledOnceWithParam<String?>(aUrl) { callback ->
            rule.setVerifySelfSessionView(
                aVerifySelfSessionState(
                    signOutAction = AsyncAction.Success(aUrl),
                    eventSink = EnsureNeverCalledWithParam(),
                ),
                onSuccessLogout = callback,
            )
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setVerifySelfSessionView(
        state: VerifySelfSessionState,
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onEnterRecoveryKey: () -> Unit = EnsureNeverCalled(),
        onFinished: () -> Unit = EnsureNeverCalled(),
        onResetKey: () -> Unit = EnsureNeverCalled(),
        onSuccessLogout: (String?) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            VerifySelfSessionView(
                state = state,
                onLearnMoreClick = onLearnMoreClick,
                onEnterRecoveryKey = onEnterRecoveryKey,
                onFinish = onFinished,
                onResetKey = onResetKey,
                onSuccessLogout = onSuccessLogout,
            )
        }
    }
}
