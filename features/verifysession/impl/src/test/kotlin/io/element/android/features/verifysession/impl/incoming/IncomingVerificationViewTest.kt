/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.verifysession.impl.R
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IncomingVerificationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    // region step Initial
    @Test
    fun `back key pressed - ignore the verification`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = aStepInitial(),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    @Test
    fun `ignore incoming verification emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = aStepInitial(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_ignore)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.IgnoreVerification)
    }

    @Test
    fun `start incoming verification emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = aStepInitial(),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_start_verification)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.StartVerification)
    }

    @Test
    fun `back key pressed - when awaiting response cancels the verification`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = aStepInitial(
                    isWaiting = true,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }
    // endregion step Initial

    // region step Verifying
    @Test
    fun `back key pressed - when ready to verify cancels the verification`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    isWaiting = false,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    @Test
    fun `back key pressed - when verifying and loading emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    isWaiting = true,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    @Test
    fun `clicking on they do not match emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    isWaiting = false,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_dont_match)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.DeclineVerification)
    }

    @Test
    fun `clicking on they match emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    isWaiting = false,
                ),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_session_verification_they_match)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.ConfirmVerification)
    }
    // endregion

    // region step Failure
    @Test
    fun `back key pressed - when failure resets the flow`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Failure,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    @Test
    fun `click on done - when failure resets the flow`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Failure,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_done)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    // endregion

    // region step Completed
    @Test
    fun `back key pressed - on Completed step emits the expected event`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Completed,
                eventSink = eventsRecorder
            ),
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }

    @Test
    fun `when flow is completed and the user clicks on the done button, the expected event is emitted`() {
        val eventsRecorder = EventsRecorder<IncomingVerificationViewEvents>()
        rule.setIncomingVerificationView(
            anIncomingVerificationState(
                step = IncomingVerificationState.Step.Completed,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_done)
        eventsRecorder.assertSingle(IncomingVerificationViewEvents.GoBack)
    }
    // endregion

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setIncomingVerificationView(
        state: IncomingVerificationState,
    ) {
        setContent {
            IncomingVerificationView(
                state = state,
            )
        }
    }
}
