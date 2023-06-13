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

package io.element.android.features.login.impl.screens.confirmaccountprovider

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.util.defaultAccountProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_OIDC
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.auth.aFakeAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfirmAccountProviderPresenterTest {
    @Test
    fun `present - initial test`() = runTest {
        val presenter = ConfirmAccountProviderPresenter(
            ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
            AccountProviderDataSource(),
            aFakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isAccountCreation).isFalse()
            assertThat(initialState.submitEnabled).isTrue()
            assertThat(initialState.accountProvider).isEqualTo(defaultAccountProvider)
            assertThat(initialState.loginFlow).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - continue password login`() = runTest {
        val authServer = aFakeAuthenticationService()
        val presenter = ConfirmAccountProviderPresenter(
            ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
            AccountProviderDataSource(),
            authServer,
        )
        authServer.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(Async.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isEqualTo(LoginFlow.PasswordLogin)
        }
    }

    @Test
    fun `present - continue oidc`() = runTest {
        val authServer = aFakeAuthenticationService()
        val presenter = ConfirmAccountProviderPresenter(
            ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
            AccountProviderDataSource(),
            authServer,
        )
        authServer.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(Async.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
        }
    }

    @Test
    fun `present - submit fails`() = runTest {
        val authServer = aFakeAuthenticationService()
        val presenter = ConfirmAccountProviderPresenter(
            ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
            AccountProviderDataSource(),
            authServer,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            authServer.givenChangeServerError(Throwable())
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val failureState = awaitItem()
            assertThat(failureState.submitEnabled).isFalse()
            assertThat(failureState.loginFlow).isInstanceOf(Async.Failure::class.java)
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val presenter = ConfirmAccountProviderPresenter(
            ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
            AccountProviderDataSource(),
            authenticationService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // Submit will return an error
            authenticationService.givenChangeServerError(A_THROWABLE)
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)

            skipItems(1) // Loading

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.loginFlow).isInstanceOf(Async.Failure::class.java)

            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginFlow).isEqualTo(Async.Uninitialized)
        }
    }
}
