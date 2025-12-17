/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressTag
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on logout sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setLogoutView(
            aLogoutState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_signout)
        eventsRecorder.assertSingle(LogoutEvents.Logout(false))
    }

    @Test
    fun `confirming logout sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(LogoutEvents.Logout(false))
    }

    @Test
    fun `clicking on back invoke back callback`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setLogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on confirm after error sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(LogoutEvents.Logout(true))
    }

    @Test
    fun `clicking on cancel after error sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(LogoutEvents.CloseDialogs)
    }

    @Test
    fun `last session setting button invoke onChangeRecoveryKeyClicked`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setLogoutView(
                aLogoutState(
                    isLastDevice = true,
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClick = callback,
            )
            rule.clickOn(CommonStrings.common_settings)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setLogoutView(
    state: LogoutState,
    onChangeRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        LogoutView(
            state = state,
            onChangeRecoveryKeyClick = onChangeRecoveryKeyClick,
            onBackClick = onBackClick,
        )
    }
}
