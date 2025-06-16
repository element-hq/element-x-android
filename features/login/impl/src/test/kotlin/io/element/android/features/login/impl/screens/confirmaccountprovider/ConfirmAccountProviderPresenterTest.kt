/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.features.login.impl.screens.onboarding.createLoginHelper
import io.element.android.features.login.impl.web.FakeWebClientUrlForAuthenticationRetriever
import io.element.android.features.login.impl.web.WebClientUrlForAuthenticationRetriever
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_OIDC
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import io.element.android.libraries.oidc.test.customtab.FakeOidcActionFlow
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
            assertThat(initialState.accountProvider.url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            assertThat(initialState.loginMode).isEqualTo(AsyncData.Uninitialized)
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isEqualTo(LoginMode.PasswordLogin)
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
        }
    }

    @Test
    fun `present - oidc - cancel with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = FakeOidcActionFlow()
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
            authenticationService.givenOidcCancelError(AN_EXCEPTION)
            defaultOidcActionFlow.post(OidcAction.GoBack)
            val cancelFailureState = awaitItem()
            assertThat(cancelFailureState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - oidc - cancel with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = FakeOidcActionFlow()
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
            defaultOidcActionFlow.post(OidcAction.GoBack)
            val cancelFinalState = awaitItem()
            assertThat(cancelFinalState.loginMode).isInstanceOf(AsyncData.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - oidc - success with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = FakeOidcActionFlow()
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
            authenticationService.givenLoginError(AN_EXCEPTION)
            defaultOidcActionFlow.post(OidcAction.Success("aUrl"))
            val cancelLoadingState = awaitItem()
            assertThat(cancelLoadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val cancelFailureState = awaitItem()
            assertThat(cancelFailureState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
        }
    }

    @Test
    fun `present - oidc - success with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val defaultOidcActionFlow = FakeOidcActionFlow()
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
            assertThat(loadingState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
            assertThat(defaultLoginUserStory.loginFlowIsDone.value).isFalse()
            defaultOidcActionFlow.post(OidcAction.Success("aUrl"))
            val successSuccessState = awaitItem()
            assertThat(successSuccessState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
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
            authenticationService.givenChangeServerError(RuntimeException())
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val failureState = awaitItem()
            assertThat(failureState.submitEnabled).isFalse()
            assertThat(failureState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
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
            authenticationService.givenChangeServerError(AN_EXCEPTION)
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)

            skipItems(1) // Loading

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.loginMode).isInstanceOf(AsyncData.Failure::class.java)

            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - confirm account creation without oidc and without url generates an error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
            webClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever {
                throw AccountCreationNotSupported()
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.loginMode.errorOrNull()).isInstanceOf(AccountCreationNotSupported::class.java)
            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - confirm account creation with oidc is successful`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val submittedState = awaitItem()
            assertThat(submittedState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(submittedState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
        }
    }

    @Test
    fun `present - confirm account creation with oidc and url continues with oidc`() = runTest {
        val aUrl = "aUrl"
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER_OIDC)
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
            webClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever { aUrl },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val submittedState = awaitItem()
            assertThat(submittedState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(submittedState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
        }
    }

    @Test
    fun `present - confirm account creation without oidc and with url continuing with url`() = runTest {
        val aUrl = "aUrl"
        val authenticationService = FakeMatrixAuthenticationService()
        authenticationService.givenHomeserver(A_HOMESERVER)
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
            webClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever { aUrl },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue)
            skipItems(1) // Loading
            val submittedState = awaitItem()
            assertThat(submittedState.loginMode.dataOrNull()).isEqualTo(LoginMode.AccountCreation(aUrl))
        }
    }

    private fun createConfirmAccountProviderPresenter(
        params: ConfirmAccountProviderPresenter.Params = ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
        accountProviderDataSource: AccountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        defaultOidcActionFlow: OidcActionFlow = FakeOidcActionFlow(),
        defaultLoginUserStory: DefaultLoginUserStory = DefaultLoginUserStory(),
        webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever = FakeWebClientUrlForAuthenticationRetriever(),
    ) = ConfirmAccountProviderPresenter(
        params = params,
        accountProviderDataSource = accountProviderDataSource,
        loginHelper = createLoginHelper(
            authenticationService = matrixAuthenticationService,
            oidcActionFlow = defaultOidcActionFlow,
            defaultLoginUserStory = defaultLoginUserStory,
            webClientUrlForAuthenticationRetriever = webClientUrlForAuthenticationRetriever,
        ),
    )
}
