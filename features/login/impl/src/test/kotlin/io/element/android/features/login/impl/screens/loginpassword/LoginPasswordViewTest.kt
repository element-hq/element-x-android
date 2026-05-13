/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.loginpassword

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class LoginPasswordViewTest {
    @Test
    fun `clicking on back invoke back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setLoginPasswordView(
                aLoginPasswordState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `changing login invokes the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = activity!!.getString(CommonStrings.common_username)
        onNodeWithText(userNameHint).performTextInput(A_USER_NAME)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin(A_USER_NAME)
        )
    }

    @Test
    fun `changing login removes new lines the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = activity!!.getString(CommonStrings.common_username)
        onNodeWithText(userNameHint).performTextInput("a\nb")
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin("ab")
        )
    }

    @Test
    fun `clearing login invokes the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(A_USER_NAME),
                eventSink = eventsRecorder,
            ),
        )
        val a11yClear = activity!!.getString(CommonStrings.action_clear)
        onNodeWithContentDescription(a11yClear).performClick()
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin("")
        )
    }

    @Test
    fun `changing password invokes the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = activity!!.getString(CommonStrings.common_password)
        onNodeWithText(userNameHint).performTextInput(A_PASSWORD)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetPassword(A_PASSWORD)
        )
    }

    @Test
    fun `reveal password makes the password visible`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithTag(TestTags.loginPassword.value).assert(hasText("••••••••"))
        val resources = activity!!.resources
        // Show password
        val a11yShowPassword = resources.getString(CommonStrings.a11y_show_password)
        onNodeWithContentDescription(a11yShowPassword).performClick()
        onNodeWithTag(TestTags.loginPassword.value).assert(hasText(A_PASSWORD))
        // Hide password
        val a11yHidePassword = resources.getString(CommonStrings.a11y_hide_password)
        onNodeWithContentDescription(a11yHidePassword).performClick()
        onNodeWithTag(TestTags.loginPassword.value).assert(hasText("••••••••"))
    }

    @Test
    fun `when login is empty, continue button is not enabled`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = activity!!.getString(CommonStrings.action_continue)
        onNodeWithText(continueStr).assertIsNotEnabled()
    }

    @Test
    fun `when password is empty, continue button is not enabled`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(login = A_USER_NAME),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = activity!!.getString(CommonStrings.action_continue)
        onNodeWithText(continueStr).assertIsNotEnabled()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Continue sends expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(login = A_USER_NAME, password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = activity!!.getString(CommonStrings.action_continue)
        onNodeWithText(continueStr).assertIsEnabled()
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.Submit
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLoginPasswordView(
    state: LoginPasswordState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        LoginPasswordView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
