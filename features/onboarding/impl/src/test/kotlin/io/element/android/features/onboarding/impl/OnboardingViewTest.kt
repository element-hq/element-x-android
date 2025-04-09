/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
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
    ) {
        setContent {
            OnBoardingView(
                state = state,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
                onReportProblem = onReportProblem,
            )
        }
    }
}
