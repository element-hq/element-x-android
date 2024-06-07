/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.onboarding.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
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
    fun `when on debug build - clicking on the settings icon opens the developer settings`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(isDebugBuild = true),
                onOpenDeveloperSettings = callback
            )
            rule.onNode(hasContentDescription(rule.activity.getString(CommonStrings.common_settings))).performClick()
        }
    }

    @Test
    fun `clicking on report a problem calls the sign in callback`() {
        ensureCalledOnce { callback ->
            rule.setOnboardingView(
                state = anOnBoardingState(),
                onReportProblem = callback,
            )
            rule.clickOn(CommonStrings.common_report_a_problem)
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setOnboardingView(
        state: OnBoardingState,
        onSignInWithQrCode: () -> Unit = EnsureNeverCalled(),
        onSignIn: () -> Unit = EnsureNeverCalled(),
        onCreateAccount: () -> Unit = EnsureNeverCalled(),
        onOpenDeveloperSettings: () -> Unit = EnsureNeverCalled(),
        onReportProblem: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            OnBoardingView(
                state = state,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
                onOpenDeveloperSettings = onOpenDeveloperSettings,
                onReportProblem = onReportProblem,
            )
        }
    }
}
