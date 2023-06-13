/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.login.impl.screens.loginpassword

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.util.defaultAccountProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.aFakeAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoginPasswordPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val accountProviderDataSource = AccountProviderDataSource()
        val presenter = LoginPasswordPresenter(
            authenticationService,
            accountProviderDataSource,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProvider).isEqualTo(defaultAccountProvider)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.loginAction).isEqualTo(Async.Uninitialized)
            assertThat(initialState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - enter login and password`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val accountProviderDataSource = AccountProviderDataSource()
        val presenter = LoginPasswordPresenter(
            authenticationService,
            accountProviderDataSource,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
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
        val authenticationService = aFakeAuthenticationService()
        val accountProviderDataSource = AccountProviderDataSource()
        val presenter = LoginPasswordPresenter(
            authenticationService,
            accountProviderDataSource,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(Async.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(Async.Success(A_SESSION_ID))
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val accountProviderDataSource = AccountProviderDataSource()
        val presenter = LoginPasswordPresenter(
            authenticationService,
            accountProviderDataSource,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(A_THROWABLE)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(Async.Loading::class.java)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loginAction).isEqualTo(Async.Failure<SessionId>(A_THROWABLE))
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val accountProviderDataSource = AccountProviderDataSource()
        val presenter = LoginPasswordPresenter(
            authenticationService,
            accountProviderDataSource,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginPasswordEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginPasswordEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(A_THROWABLE)
            loginAndPasswordState.eventSink.invoke(LoginPasswordEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(Async.Loading::class.java)
            val loggedInState = awaitItem()
            // Check an error was returned
            assertThat(loggedInState.loginAction).isEqualTo(Async.Failure<SessionId>(A_THROWABLE))
            // Assert the error is then cleared
            loggedInState.eventSink(LoginPasswordEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginAction).isEqualTo(Async.Uninitialized)
        }
    }
}
