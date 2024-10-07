/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.oidc.impl.webview

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.auth.A_OIDC_DATA
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OidcPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            FakeMatrixAuthenticationService(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.oidcDetails).isEqualTo(A_OIDC_DATA)
            assertThat(initialState.requestState).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - go back`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            FakeMatrixAuthenticationService(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.Cancel)
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - go back with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            authenticationService,
        )
        authenticationService.givenOidcCancelError(A_THROWABLE)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.Cancel)
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            // Note: in real life I do not think this can happen, and the app should not block the user.
        }
    }

    @Test
    fun `present - user cancels from webview`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            FakeMatrixAuthenticationService(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.GoBack))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - login success`() = runTest {
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            FakeMatrixAuthenticationService(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.Success("A_URL")))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(AsyncAction.Loading)
            // In this case, no success, the session is created and the node get destroyed.
        }
    }

    @Test
    fun `present - login error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = OidcPresenter(
            A_OIDC_DATA,
            authenticationService,
        )
        authenticationService.givenLoginError(A_THROWABLE)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(OidcEvents.OidcActionEvent(OidcAction.Success("A_URL")))
            val loadingState = awaitItem()
            assertThat(loadingState.requestState).isEqualTo(AsyncAction.Loading)
            val errorState = awaitItem()
            assertThat(errorState.requestState).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            errorState.eventSink.invoke(OidcEvents.ClearError)
            val finalState = awaitItem()
            assertThat(finalState.requestState).isEqualTo(AsyncAction.Uninitialized)
        }
    }
}
