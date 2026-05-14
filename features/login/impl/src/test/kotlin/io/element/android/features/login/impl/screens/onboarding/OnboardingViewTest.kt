/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class OnboardingViewTest {
    @Test
    fun `when can create account - clicking on create account calls the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    canCreateAccount = true,
                    showDeveloperSettings = false,
                    eventSink = eventSink,
                ),
                onCreateAccount = callback,
            )
            clickOn(R.string.screen_onboarding_sign_up)
            // Developer settings should not be shown
            val developerSettingsText = activity!!.getString(CommonStrings.common_developer_options)
            onNodeWithContentDescription(developerSettingsText).assertDoesNotExist()
        }
    }

    @Test
    fun `when can go back - clicking on back calls the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    isAddingAccount = true,
                    eventSink = eventSink,
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `when can login with QR code - clicking on sign in with QR code calls the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    canLoginWithQrCode = true,
                    eventSink = eventSink,
                ),
                onSignInWithQrCode = callback,
            )
            clickOn(R.string.screen_onboarding_sign_in_with_qr_code)
        }
    }

    @Test
    fun `when can login with QR code - clicking on sign in manually calls the expected callback`(
        @TestParameter mustChooseAccountProvider: Boolean = namedTestValues(
            "can search account provider" to false,
            "cannot search account provider" to true,
        )
    ) = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnceWithParam(mustChooseAccountProvider) { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    canLoginWithQrCode = true,
                    mustChooseAccountProvider = mustChooseAccountProvider,
                    eventSink = eventSink,
                ),
                onSignIn = callback,
            )
            clickOn(R.string.screen_onboarding_sign_in_manually)
        }
    }

    @Test
    fun `when cannot login with QR code or create account - clicking on continue calls the sign in callback`(
        @TestParameter mustChooseAccountProvider: Boolean = namedTestValues(
            "can search account provider" to false,
            "cannot search account provider" to true,
        )
    ) = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnceWithParam(mustChooseAccountProvider) { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    canLoginWithQrCode = false,
                    canCreateAccount = false,
                    mustChooseAccountProvider = mustChooseAccountProvider,
                    eventSink = eventSink,
                ),
                onSignIn = callback,
            )
            clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `when sign in to pre defined account provider - clicking on button emits the expected event`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        setOnboardingView(
            state = anOnBoardingState(
                defaultAccountProvider = "element.io",
                eventSink = eventSink,
            ),
        )
        val buttonText = activity!!.getString(R.string.screen_onboarding_sign_in_to, "element.io")
        onNodeWithText(buttonText).performClick()
        eventSink.assertSingle(OnBoardingEvents.OnSignIn("element.io"))
    }

    @Test
    fun `when error is displayed - closing the dialog emits the expected event`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        setOnboardingView(
            state = anOnBoardingState(
                defaultAccountProvider = "element.io",
                loginMode = AsyncData.Failure(AN_EXCEPTION),
                eventSink = eventSink,
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventSink.assertSingle(OnBoardingEvents.ClearError)
    }

    @Test
    fun `clicking on report a problem calls the sign in callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    canReportBug = true,
                    eventSink = eventSink,
                ),
                onReportProblem = callback,
            )
            val text = activity!!.getString(CommonStrings.common_report_a_problem)
            onNodeWithText(text).assertExists()
            clickOn(CommonStrings.common_report_a_problem)
        }
    }

    @Test
    fun `clicking on settings calls the developer settings callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    showDeveloperSettings = true,
                    eventSink = eventSink,
                ),
                onDeveloperSettingsClick = callback,
            )
            val text = activity!!.getString(CommonStrings.common_developer_options)
            onNodeWithContentDescription(text).performClick()
        }
    }

    @Test
    fun `cannot report a problem when the feature is disabled`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>(expectEvents = false)
        setOnboardingView(
            state = anOnBoardingState(
                canReportBug = false,
                eventSink = eventSink,
            ),
        )
        val text = activity!!.getString(CommonStrings.common_report_a_problem)
        onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun `when success PasswordLogin - the expected callback is invoked and the event is received`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        ensureCalledOnce { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    loginMode = AsyncData.Success(LoginMode.PasswordLogin),
                    eventSink = eventSink,
                ),
                onNeedLoginPassword = callback,
            )
        }
        eventSink.assertSingle(OnBoardingEvents.ClearError)
    }

    @Test
    fun `when success Oidc - the expected callback is invoked and the event is received`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        val oAuthDetails = OAuthDetails("aUrl")
        ensureCalledOnceWithParam(oAuthDetails) { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    loginMode = AsyncData.Success(LoginMode.OAuth(oAuthDetails)),
                    eventSink = eventSink,
                ),
                onOAuthDetails = callback,
            )
        }
        eventSink.assertSingle(OnBoardingEvents.ClearError)
    }

    @Test
    fun `when success AccountCreation - the expected callback is invoked and the event is received`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<OnBoardingEvents>()
        val oAuthDetails = OAuthDetails("aUrl")
        ensureCalledOnceWithParam(oAuthDetails.url) { callback ->
            setOnboardingView(
                state = anOnBoardingState(
                    loginMode = AsyncData.Success(LoginMode.AccountCreation("aUrl")),
                    eventSink = eventSink,
                ),
                onCreateAccountContinue = callback,
            )
        }
        eventSink.assertSingle(OnBoardingEvents.ClearError)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setOnboardingView(
        state: OnBoardingState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onDeveloperSettingsClick: () -> Unit = EnsureNeverCalled(),
        onSignInWithQrCode: () -> Unit = EnsureNeverCalled(),
        onSignIn: (Boolean) -> Unit = EnsureNeverCalledWithParam(),
        onCreateAccount: () -> Unit = EnsureNeverCalled(),
        onReportProblem: () -> Unit = EnsureNeverCalled(),
        onOAuthDetails: (OAuthDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onCreateAccountContinue: (url: String) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            OnBoardingView(
                state = state,
                onBackClick = onBackClick,
                onDeveloperSettingsClick = onDeveloperSettingsClick,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
                onReportProblem = onReportProblem,
                onOAuthDetails = onOAuthDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
                onCreateAccountContinue = onCreateAccountContinue,
            )
        }
    }
}
