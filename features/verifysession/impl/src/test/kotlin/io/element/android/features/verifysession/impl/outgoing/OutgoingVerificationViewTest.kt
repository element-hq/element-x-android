/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
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

@RunWith(AndroidJUnit4::class)
class OutgoingVerificationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `back key pressed - when canceled resets the flow`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Canceled,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Reset)
    }

    @Test
    fun `back key pressed - when awaiting response cancels the verification`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.AwaitingOtherDeviceResponse,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when ready to verify cancels the verification`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Ready,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when verifying and not loading declines the verification`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.DeclineVerification)
    }

    @Test
    fun `back key pressed - when verifying and loading does nothing`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
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
    fun `back key pressed - on Completed exits the flow`() {
        ensureCalledOnce { callback ->
            rule.setOutgoingVerificationView(
                onBack = callback,
                state = anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                ),
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `when flow is completed and the user clicks on the continue button, the expected callback is invoked`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setOutgoingVerificationView(
                anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                    eventSink = eventsRecorder
                ),
                onFinished = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `clicking on they match emits the expected event`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.ConfirmVerification)
    }

    @Test
    fun `clicking on they do not match emits the expected event`() {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        rule.setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_dont_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.DeclineVerification)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setOutgoingVerificationView(
        state: OutgoingVerificationState,
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onFinished: () -> Unit = EnsureNeverCalled(),
        onBack: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            OutgoingVerificationView(
                state = state,
                onLearnMoreClick = onLearnMoreClick,
                onFinish = onFinished,
                onBack = onBack,
            )
        }
    }
}
