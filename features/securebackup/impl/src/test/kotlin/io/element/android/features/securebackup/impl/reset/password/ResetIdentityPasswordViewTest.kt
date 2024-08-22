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

package io.element.android.features.securebackup.impl.reset.password

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetIdentityPasswordViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pressing the back HW button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetPasswordView(
                ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = {}),
                onBack = it,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetPasswordView(
                ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = {}),
                onBack = it,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking 'Reset identity' confirms the reset`() {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        rule.setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = eventsRecorder),
        )
        rule.onNodeWithText("Password").performTextInput("A password")

        rule.clickOn(CommonStrings.action_reset_identity)

        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.Reset("A password"))
    }

    @Test
    fun `clicking on confirmation dialog 'Yes, reset now' confirms the reset`() {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        rule.setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Confirming, eventSink = eventsRecorder),
        )
        rule.onNodeWithText("Password").performTextInput("A password")
        rule.clickOn(R.string.screen_reset_encryption_confirmation_alert_action)
        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.Reset("A password"))
    }

    @Test
    fun `clicking on confirmation dialog 'Cancel' emits the expected event`() {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        rule.setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Confirming, eventSink = eventsRecorder),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.DismissConfirmationDialog)
    }

    @Test
    fun `modifying the password dismisses the error state`() {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        rule.setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Failure(IllegalStateException("A failure")), eventSink = eventsRecorder),
        )
        rule.onNodeWithText("Password").performTextInput("A password")

        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.DismissError)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setResetPasswordView(
    state: ResetIdentityPasswordState,
    onBack: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityPasswordView(state = state, onBack = onBack)
    }
}
