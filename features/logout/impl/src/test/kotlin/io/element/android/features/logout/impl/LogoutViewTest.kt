/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutViewTest {
    @Test
    fun `clicking on logout sends a LogoutEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        setLogoutView(
            aLogoutState(
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_signout)
        eventsRecorder.assertSingle(LogoutEvents.Logout(false))
    }

    @Test
    fun `confirming logout sends a LogoutEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(LogoutEvents.Logout(false))
    }

    @Test
    fun `clicking on back invoke back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on confirm after error sends a LogoutEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(LogoutEvents.Logout(true))
    }

    @Test
    fun `clicking on cancel after error sends a LogoutEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        setLogoutView(
            aLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(LogoutEvents.CloseDialogs)
    }

    @Test
    fun `last session setting button invoke onChangeRecoveryKeyClicked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLogoutView(
                aLogoutState(
                    isLastDevice = true,
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClick = callback,
            )
            clickOn(CommonStrings.common_settings)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLogoutView(
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
