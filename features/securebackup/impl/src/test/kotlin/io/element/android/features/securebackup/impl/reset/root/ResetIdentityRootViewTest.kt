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

package io.element.android.features.securebackup.impl.reset.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securebackup.impl.R
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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ResetIdentityRootViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pressing the back HW button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = false, eventSink = {}),
                onBack = it,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() {
        ensureCalledOnce {
            rule.setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = false, eventSink = {}),
                onBack = it,
            )
            rule.pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `clicking Continue displays the confirmation dialog`() {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        rule.setResetRootView(
            ResetIdentityRootState(displayConfirmationDialog = false, eventSink = eventsRecorder),
        )

        rule.clickOn(CommonStrings.action_continue)

        eventsRecorder.assertSingle(ResetIdentityRootEvent.Continue)
    }

    @Test
    fun `clicking 'Yes, reset now' confirms the reset`() {
        ensureCalledOnce {
            rule.setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = true, eventSink = {}),
                onContinue = it,
            )
            rule.clickOn(R.string.screen_reset_encryption_confirmation_alert_action)
        }
    }

    @Test
    fun `clicking Cancel dismisses the dialog`() {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        rule.setResetRootView(
            ResetIdentityRootState(displayConfirmationDialog = true, eventSink = eventsRecorder),
        )

        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ResetIdentityRootEvent.DismissDialog)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setResetRootView(
    state: ResetIdentityRootState,
    onBack: () -> Unit = EnsureNeverCalled(),
    onContinue: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityRootView(state = state, onContinue = onContinue, onBack = onBack)
    }
}
