/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class LoginPasswordViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke back callback`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setLoginPasswordView(
                aLoginPasswordState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `changing login invokes the expected event`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        rule.setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = rule.activity.getString(CommonStrings.common_username)
        rule.onNodeWithText(userNameHint).performTextInput(A_USER_NAME)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin(A_USER_NAME)
        )
    }

    @Test
    fun `changing login removes new lines the expected event`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        rule.setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = rule.activity.getString(CommonStrings.common_username)
        rule.onNodeWithText(userNameHint).performTextInput("a\nb")
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin("ab")
        )
    }

    @Test
    fun `clearing login invokes the expected event`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        rule.setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(A_USER_NAME),
                eventSink = eventsRecorder,
            ),
        )
        val a11yClear = rule.activity.getString(CommonStrings.action_clear)
        rule.onNodeWithContentDescription(a11yClear).performClick()
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetLogin("")
        )
    }

    @Test
    fun `changing password invokes the expected event`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        rule.setLoginPasswordView(
            aLoginPasswordState(
                eventSink = eventsRecorder,
            ),
        )
        val userNameHint = rule.activity.getString(CommonStrings.common_password)
        rule.onNodeWithText(userNameHint).performTextInput(A_PASSWORD)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.SetPassword(A_PASSWORD)
        )
    }

    @Test
    fun `reveal password makes the password visible`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        rule.setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        rule.onNodeWithTag(TestTags.loginPassword.value).assert(hasText("••••••••"))
        // Show password
        val a11yShowPassword = rule.activity.getString(CommonStrings.a11y_show_password)
        rule.onNodeWithContentDescription(a11yShowPassword).performClick()
        rule.onNodeWithTag(TestTags.loginPassword.value).assert(hasText(A_PASSWORD))
        // Hide password
        val a11yHidePassword = rule.activity.getString(CommonStrings.a11y_hide_password)
        rule.onNodeWithContentDescription(a11yHidePassword).performClick()
        rule.onNodeWithTag(TestTags.loginPassword.value).assert(hasText("••••••••"))
    }

    @Test
    fun `when login is empty, continue button is not enabled`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        rule.setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = rule.activity.getString(CommonStrings.action_continue)
        rule.onNodeWithText(continueStr).assertIsNotEnabled()
    }

    @Test
    fun `when password is empty, continue button is not enabled`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>(expectEvents = false)
        rule.setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(login = A_USER_NAME),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = rule.activity.getString(CommonStrings.action_continue)
        rule.onNodeWithText(continueStr).assertIsNotEnabled()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Continue sends expected event`() {
        val eventsRecorder = EventsRecorder<LoginPasswordEvents>()
        rule.setLoginPasswordView(
            aLoginPasswordState(
                formState = aLoginFormState(login = A_USER_NAME, password = A_PASSWORD),
                eventSink = eventsRecorder,
            ),
        )
        val continueStr = rule.activity.getString(CommonStrings.action_continue)
        rule.onNodeWithText(continueStr).assertIsEnabled()
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(
            LoginPasswordEvents.Submit
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setLoginPasswordView(
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
