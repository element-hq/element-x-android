/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `when can create account - clicking on create account calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(canCreateAccount = true),
                onCreateAccount = callback,
            )
            rule.clickOn(R.string.screen_onboarding_sign_up)
        }
    }

    @Test
    fun `when can login with QR code - clicking on sign in with QR code calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(canLoginWithQrCode = true),
                onSignInWithQrCode = callback,
            )
            rule.clickOn(R.string.screen_onboarding_sign_in_with_qr_code)
        }
    }

    @Test
    fun `when can login with QR code - clicking on sign in manually calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(canLoginWithQrCode = true),
                onSignIn = callback,
            )
            rule.clickOn(R.string.screen_onboarding_sign_in_manually)
        }
    }

    @Test
    fun `when cannot login with QR code or create account - clicking on continue calls the sign in callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(
                    canLoginWithQrCode = false,
                    canCreateAccount = false,
                ),
                onSignIn = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `when sign in to pre defined account provider - clicking on button emits the expected event`() {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        rule.setOnboardingView(
            state = anOnBoardingState(
                defaultAccountProvider = "element.io",
                eventSink = eventSink,
            ),
        )
        val buttonText = rule.activity.getString(R.string.screen_onboarding_sign_in_to, "element.io")
        rule.onNodeWithText(buttonText).performClick()
        eventSink.assertSingle(OnBoardingEvents.OnSignIn("element.io"))
    }

    @Test
    fun `when error is displayed - closing the dialog emits the expected event`() {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        rule.setOnboardingView(
            state = anOnBoardingState(
                defaultAccountProvider = "element.io",
                loginMode = AsyncData.Failure(AN_EXCEPTION),
                eventSink = eventSink,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventSink.assertSingle(OnBoardingEvents.ClearError)
    }

    @Test
    fun `clicking on report a problem calls the sign in callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(
                    canReportBug = true,
                ),
                onReportProblem = callback,
            )
            val text = rule.activity.getString(CommonStrings.common_report_a_problem)
            rule.onNodeWithText(text).assertExists()
            rule.clickOn(CommonStrings.common_report_a_problem)
        }
    }

    @Test
    fun `cannot report a problem when the feature is disabled`() {
        rule.setOnboardingView(
            state = anOnBoardingState(
                canReportBug = false,
            ),
        )
        val text = rule.activity.getString(CommonStrings.common_report_a_problem)
        rule.onNodeWithText(text).assertDoesNotExist()
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setOnboardingView(
        state: OnBoardingState,
        onSignInWithQrCode: () -> Unit = EnsureNeverCalled(),
        onSignIn: () -> Unit = EnsureNeverCalled(),
        onCreateAccount: () -> Unit = EnsureNeverCalled(),
        onReportProblem: () -> Unit = EnsureNeverCalled(),
        onOidcDetails: (OidcDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onCreateAccountContinue: (url: String) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            OnBoardingView(
                state = state,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
                onReportProblem = onReportProblem,
                onOidcDetails = onOidcDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
                onCreateAccountContinue = onCreateAccountContinue,
            )
        }
    }
}
