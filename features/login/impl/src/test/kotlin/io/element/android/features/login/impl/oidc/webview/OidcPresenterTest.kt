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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.login.impl.oidc.webview

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.auth.A_OIDC_DATA
import io.element.android.libraries.matrix.test.auth.aFakeAuthenticationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OidcPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            aFakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.oidcDetails).isEqualTo(A_OIDC_DATA)
            assertThat(initialState.requestState).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - go back`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            aFakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.Cancel)
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(Async.Loading<Unit>())
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(Async.Success(Unit))
        }
    }

    @Test
    fun `present - go back with failure`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            authenticationService,
        )
        authenticationService.givenOidcCancelError(A_THROWABLE)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.Cancel)
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(Async.Loading<Unit>())
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(Async.Failure<Unit>(A_THROWABLE))
            // Note: in real life I do not think this can happen, and the app should not block the user.
        }
    }

    @Test
    fun `present - user cancels from webview`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            aFakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.GoBack))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(Async.Loading<Unit>())
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(Async.Success(Unit))
        }
    }

    @Test
    fun `present - login success`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            aFakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.Success("A_URL")))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(Async.Loading<Unit>())
            // In this case, no success, the session is created and the node get destroyed.
        }
    }

    @Test
    fun `present - login error`() = runTest {
        val authenticationService = aFakeAuthenticationService()
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            authenticationService,
        )
        authenticationService.givenLoginError(A_THROWABLE)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.Success("A_URL")))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(Async.Loading<Unit>())
            val errorState = awaitItem()
            assertThat(errorState.requestState).isEqualTo(Async.Failure<Unit>(A_THROWABLE))
            errorState.eventSink.invoke(OidcEvents.ClearError)
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(Async.Uninitialized)
        }
    }
}
