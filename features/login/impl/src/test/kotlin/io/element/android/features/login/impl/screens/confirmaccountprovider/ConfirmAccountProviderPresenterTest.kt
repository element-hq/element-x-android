/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.util.defaultAccountProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_OIDC
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.impl.customtab.DefaultOidcActionFlow
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.waitForPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConfirmAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial test`() = runTest {
        val presenter = createConfirmAccountProviderPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isAccountCreation).isFalse()
            assertThat(initialState.submitEnabled).isTrue()
            assertThat(initialState.accountProvider).isEqualTo(defaultAccountProvider)
            assertThat(initialState.loginFlow).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - continue password login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        authenticationService.givenHomeserver(A_HOMESERVER)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isEqualTo(LoginFlow.PasswordLogin)
        }
    }

    @Test
    fun `present - continue oidc`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
        }
    }

    @Test
    fun `present - oidc - cancel with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = DefaultOidcActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOidcActionFlow = defaultOidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
            authenticationService.givenOidcCancelError(A_THROWABLE)
            defaultOidcActionFlow.post(OidcAction.GoBack)
            val cancelFailureState = awaitItem()
            assertThat(cancelFailureState.loginFlow).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - oidc - cancel with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = DefaultOidcActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOidcActionFlow = defaultOidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
            defaultOidcActionFlow.post(OidcAction.GoBack)
            val cancelFinalState = awaitItem()
            assertThat(cancelFinalState.loginFlow).isInstanceOf(AsyncData.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - oidc - success with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = DefaultOidcActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOidcActionFlow = defaultOidcActionFlow,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
            authenticationService.givenLoginError(A_THROWABLE)
            defaultOidcActionFlow.post(OidcAction.Success("aUrl"))
            val cancelLoadingState = awaitItem()
            assertThat(cancelLoadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val cancelFailureState = awaitItem()
            assertThat(cancelFailureState.loginFlow).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - oidc - success with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = DefaultOidcActionFlow()
        val defaultLoginUserStory = DefaultLoginUserStory().apply {
            setLoginFlowIsDone(false)
        }
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOidcActionFlow = defaultOidcActionFlow,
            defaultLoginUserStory = defaultLoginUserStory,
        )
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isTrue()
            assertThat(loadingState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginFlow).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginFlow.dataOrNull()).isInstanceOf(LoginFlow.OidcFlow::class.java)
            assertThat(defaultLoginUserStory.loginFlowIsDone.value).isFalse()
            defaultOidcActionFlow.post(OidcAction.Success("aUrl"))
            val successSuccessState = awaitItem()
            assertThat(successSuccessState.loginFlow).isInstanceOf(AsyncData.Loading::class.java)
            waitForPredicate { defaultLoginUserStory.loginFlowIsDone.value }
        }
    }

    @Test
    fun `present - submit fails`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            authenticationService.givenChangeServerError(Throwable())
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val failureState = awaitItem()
            assertThat(failureState.submitEnabled).isFalse()
            assertThat(failureState.loginFlow).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // Submit will return an error
            authenticationService.givenChangeServerError(A_THROWABLE)
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)

            skipItems(1) // Loading

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.loginFlow).isInstanceOf(AsyncData.Failure::class.java)

            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginFlow).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createConfirmAccountProviderPresenter(
        params: ConfirmAccountProviderPresenter.Params = ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
        accountProviderDataSource: AccountProviderDataSource = AccountProviderDataSource(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        defaultOidcActionFlow: DefaultOidcActionFlow = DefaultOidcActionFlow(),
        defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory(),
    ) = ConfirmAccountProviderPresenter(
        params = params,
        accountProviderDataSource = accountProviderDataSource,
        authenticationService = matrixAuthenticationService,
        oidcActionFlow = defaultOidcActionFlow,
        defaultLoginUserStory = defaultLoginUserStory,
    )
}
