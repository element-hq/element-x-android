/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

        rule.clickOn(R.string.screen_encryption_reset_action_continue_reset)

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
