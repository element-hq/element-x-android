/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.verifysession.impl.outgoing

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OutgoingVerificationViewTest {
    @Test
    fun `back key pressed - when canceled resets the flow`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Canceled,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Reset)
    }

    @Test
    fun `back key pressed - when awaiting response cancels the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.AwaitingOtherDeviceResponse,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when ready to verify cancels the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Ready,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.Cancel)
    }

    @Test
    fun `back key pressed - when verifying and not loading declines the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.DeclineVerification)
    }

    @Test
    fun `back key pressed - when verifying and loading does nothing`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Loading(),
                ),
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `back key pressed - on Completed exits the flow`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setOutgoingVerificationView(
                onBack = callback,
                state = anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                ),
            )
            pressBackKey()
        }
    }

    @Test
    fun `when flow is completed and the user clicks on the continue button, the expected callback is invoked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOutgoingVerificationView(
                anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                    eventSink = eventsRecorder
                ),
                onFinished = callback,
            )
            clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `clicking on they match emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_session_verification_they_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.ConfirmVerification)
    }

    @Test
    fun `clicking on they do not match emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvents>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_session_verification_they_dont_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvents.DeclineVerification)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setOutgoingVerificationView(
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
