/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.loginpassword

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoginPasswordPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createLoginPasswordPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.accountProvider.url).isEqualTo(FakeEnterpriseService.A_FAKE_HOMESERVER)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.loginAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - enter login and password`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            assertThat(loginState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = ""))
            assertThat(loginState.submitEnabled).isFalse()
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            val loginAndPasswordState = awaitItem()
            assertThat(loginAndPasswordState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = A_PASSWORD))
            assertThat(loginAndPasswordState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        val loginUserStory = DefaultLoginUserStory().apply { setLoginFlowIsDone(false) }
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
            defaultLoginUserStory = loginUserStory,
        ).test {
            assertThat(loginUserStory.loginFlowIsDone.value).isFalse()
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Success(A_SESSION_ID))
            assertThat(loginUserStory.loginFlowIsDone.value).isTrue()
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(A_THROWABLE)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Failure<SessionId>(A_THROWABLE))
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        createLoginPasswordPresenter(
            authenticationService = authenticationService,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(A_THROWABLE)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val loggedInState = awaitItem()
            // Check an error was returned
            assertThat(loggedInState.loginAction).isEqualTo(AsyncData.Failure<SessionId>(A_THROWABLE))
            // Assert the error is then cleared
            loggedInState.eventSink(LoginPasswordEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createLoginPasswordPresenter(
        authenticationService: FakeMatrixAuthenticationService = FakeMatrixAuthenticationService(),
        accountProviderDataSource: AccountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
        defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory()
    ): LoginPasswordPresenter = LoginPasswordPresenter(
        authenticationService = authenticationService,
        accountProviderDataSource = accountProviderDataSource,
        defaultLoginUserStory = defaultLoginUserStory,
    )
}
