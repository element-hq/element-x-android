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

package io.element.android.features.login.impl.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.impl.oidc.customtab.DefaultOidcActionFlow
import io.element.android.features.login.impl.util.LoginConstants
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_OIDC
import io.element.android.libraries.matrix.test.A_PASSWORD
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.auth.A_OIDC_DATA
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoginRootPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = LoginRootPresenter(
            FakeAuthenticationService(),
            DefaultOidcActionFlow(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.homeserverUrl).isEqualTo(LoginConstants.DEFAULT_HOMESERVER_URL)
            assertThat(initialState.homeserverDetails).isEqualTo(Async.Uninitialized)
            assertThat(initialState.loggedInState).isEqualTo(LoggedInState.NotLoggedIn)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.submitEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state server load`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.homeserverUrl).isEqualTo(LoginConstants.DEFAULT_HOMESERVER_URL)
            assertThat(initialState.homeserverDetails).isEqualTo(Async.Uninitialized)
            assertThat(initialState.loggedInState).isEqualTo(LoggedInState.NotLoggedIn)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.submitEnabled).isFalse()
            val loadingState = awaitItem()
            assertThat(loadingState.homeserverDetails).isEqualTo(Async.Loading<MatrixHomeServerDetails>())
            authenticationService.givenHomeserver(A_HOMESERVER)
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.homeserverDetails).isEqualTo(Async.Success(A_HOMESERVER))
        }
    }

    @Test
    fun `present - initial state server load error and retry`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.homeserverUrl).isEqualTo(LoginConstants.DEFAULT_HOMESERVER_URL)
            assertThat(initialState.homeserverDetails).isEqualTo(Async.Uninitialized)
            assertThat(initialState.loggedInState).isEqualTo(LoggedInState.NotLoggedIn)
            assertThat(initialState.formState).isEqualTo(LoginFormState.Default)
            assertThat(initialState.submitEnabled).isFalse()
            val loadingState = awaitItem()
            assertThat(loadingState.homeserverDetails).isEqualTo(Async.Loading<MatrixHomeServerDetails>())
            val aThrowable = Throwable("Error")
            authenticationService.givenChangeServerError(aThrowable)
            val errorState = awaitItem()
            assertThat(errorState.homeserverDetails).isEqualTo(Async.Failure<MatrixHomeServerDetails>(aThrowable))
            // Retry
            errorState.eventSink.invoke(LoginRootEvents.RetryFetchServerInfo)
            val loadingState2 = awaitItem()
            assertThat(loadingState2.homeserverDetails).isEqualTo(Async.Loading<MatrixHomeServerDetails>())
            authenticationService.givenChangeServerError(null)
            authenticationService.givenHomeserver(A_HOMESERVER)
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.homeserverDetails).isEqualTo(Async.Success(A_HOMESERVER))
        }
    }

    @Test
    fun `present - enter login and password`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginRootEvents.SetLogin(A_USER_NAME))
            val loginState = awaitItem()
            assertThat(loginState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = ""))
            assertThat(loginState.submitEnabled).isFalse()
            initialState.eventSink.invoke(LoginRootEvents.SetPassword(A_PASSWORD))
            val loginAndPasswordState = awaitItem()
            assertThat(loginAndPasswordState.formState).isEqualTo(LoginFormState(login = A_USER_NAME, password = A_PASSWORD))
            assertThat(loginAndPasswordState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - oidc login`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.submitEnabled).isTrue()
            initialState.eventSink.invoke(LoginRootEvents.Submit)
            val oidcState = awaitItem()
            assertThat(oidcState.loggedInState).isEqualTo(LoggedInState.OidcStarted(A_OIDC_DATA))
        }
    }

    @Test
    fun `present - oidc login error`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        authenticationService.givenOidcError(A_THROWABLE)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.submitEnabled).isTrue()
            initialState.eventSink.invoke(LoginRootEvents.Submit)
            val oidcState = awaitItem()
            assertThat(oidcState.loggedInState).isEqualTo(LoggedInState.ErrorLoggingIn(A_THROWABLE))
        }
    }

    @Test
    fun `present - oidc custom tab login`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.submitEnabled).isTrue()
            initialState.eventSink.invoke(LoginRootEvents.Submit)
            val oidcState = awaitItem()
            assertThat(oidcState.loggedInState).isEqualTo(LoggedInState.OidcStarted(A_OIDC_DATA))
            // Oidc cancel, sdk error
            authenticationService.givenOidcCancelError(A_THROWABLE)
            oidcActionFlow.post(OidcAction.GoBack)
            val stateCancelSdkError = awaitItem()
            assertThat(stateCancelSdkError.loggedInState).isEqualTo(LoggedInState.ErrorLoggingIn(A_THROWABLE))
            // Oidc cancel, sdk OK
            authenticationService.givenOidcCancelError(null)
            oidcActionFlow.post(OidcAction.GoBack)
            val stateCancel = awaitItem()
            assertThat(stateCancel.loggedInState).isEqualTo(LoggedInState.NotLoggedIn)
            // Oidc success, sdk error
            authenticationService.givenLoginError(A_THROWABLE)
            oidcActionFlow.post(OidcAction.Success(A_OIDC_DATA.url))
            val stateSuccessSdkErrorLoading = awaitItem()
            assertThat(stateSuccessSdkErrorLoading.loggedInState).isEqualTo(LoggedInState.LoggingIn)
            val stateSuccessSdkError = awaitItem()
            assertThat(stateSuccessSdkError.loggedInState).isEqualTo(LoggedInState.ErrorLoggingIn(A_THROWABLE))
            // Oidc success
            authenticationService.givenLoginError(null)
            oidcActionFlow.post(OidcAction.Success(A_OIDC_DATA.url))
            val stateSuccess = awaitItem()
            assertThat(stateSuccess.loggedInState).isEqualTo(LoggedInState.LoggingIn)
            val stateSuccessLoggedIn = awaitItem()
            assertThat(stateSuccessLoggedIn.loggedInState).isEqualTo(LoggedInState.LoggedIn(A_SESSION_ID))
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginRootEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginRootEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            loginAndPasswordState.eventSink.invoke(LoginRootEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loggedInState).isEqualTo(LoggedInState.LoggingIn)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loggedInState).isEqualTo(LoggedInState.LoggedIn(A_SESSION_ID))
        }
    }

    @Test
    fun `present - submit with error`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LoginRootEvents.SetLogin(A_USER_NAME))
            initialState.eventSink.invoke(LoginRootEvents.SetPassword(A_PASSWORD))
            skipItems(1)
            val loginAndPasswordState = awaitItem()
            authenticationService.givenLoginError(A_THROWABLE)
            loginAndPasswordState.eventSink.invoke(LoginRootEvents.Submit)
            val submitState = awaitItem()
            assertThat(submitState.loggedInState).isEqualTo(LoggedInState.LoggingIn)
            val loggedInState = awaitItem()
            assertThat(loggedInState.loggedInState).isEqualTo(LoggedInState.ErrorLoggingIn(A_THROWABLE))
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val oidcActionFlow = DefaultOidcActionFlow()
        val presenter = LoginRootPresenter(
            authenticationService,
            oidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Submit will return an error
            authenticationService.givenLoginError(A_THROWABLE)
            initialState.eventSink(LoginRootEvents.Submit)
            awaitItem() // Skip LoggingIn state

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.loggedInState).isEqualTo(LoggedInState.ErrorLoggingIn(A_THROWABLE))

            // Assert the error is then cleared
            submittedState.eventSink(LoginRootEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loggedInState).isEqualTo(LoggedInState.NotLoggedIn)
        }
    }
}
