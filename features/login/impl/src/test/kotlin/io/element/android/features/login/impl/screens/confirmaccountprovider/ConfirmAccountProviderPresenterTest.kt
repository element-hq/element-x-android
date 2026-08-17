/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.accountprovider.anAccountProviderDataSource
import io.element.android.features.login.impl.changeserver.ChangeServerPresenter
import io.element.android.features.login.impl.localnetwork.LocalNetworkPermissionGate
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.features.login.impl.screens.onboarding.createLoginModePresenter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.aMatrixHomeServerDetails
import io.element.android.libraries.oauth.api.OAuthAction
import io.element.android.libraries.oauth.api.OAuthActionFlow
import io.element.android.libraries.oauth.test.customtab.FakeOAuthActionFlow
import io.element.android.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConfirmAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial test`() = runTest {
        val presenter = createConfirmAccountProviderPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isAccountCreation).isFalse()
            assertThat(initialState.submitEnabled).isTrue()
            assertThat(initialState.accountProviderInput).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            assertThat(initialState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - continue password login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isEqualTo(LoginMode.PasswordLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue oidc`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            authenticationService.givenOAuthCancelError(AN_EXCEPTION)
            defaultOAuthActionFlow.post(OAuthAction.GoBack())
            val cancelFailureState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(cancelFailureState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            defaultOAuthActionFlow.post(OAuthAction.GoBack())
            val cancelFinalState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(cancelFinalState.loginModeState.loginMode).isInstanceOf(AsyncData.Uninitialized::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - cancel to unblock`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            awaitLoginMode { it is AsyncData.Loading }
            defaultOAuthActionFlow.post(OAuthAction.GoBack(toUnblock = true))
            val cancelFinalState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(cancelFinalState.loginModeState.loginMode).isInstanceOf(AsyncData.Uninitialized::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - success with failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOAuthActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOAuthActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            authenticationService.givenLoginError(AN_EXCEPTION)
            defaultOAuthActionFlow.post(OAuthAction.Success("aUrl"))
            val cancelFailureState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(cancelFailureState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - OAuth - success with success`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val defaultOidcActionFlow = FakeOAuthActionFlow()
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            defaultOAuthActionFlow = defaultOidcActionFlow,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val successState = awaitLoginMode { it is AsyncData.Success }
            assertThat(successState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            defaultOidcActionFlow.post(OAuthAction.Success("aUrl"))
            val successSuccessState = awaitLoginMode { it is AsyncData.Loading }
            assertThat(successSuccessState.loginModeState.loginMode).isInstanceOf(AsyncData.Loading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - submit fails`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            // The account provider validation fails, so the login is never attempted.
            val failureState = awaitState { it.changeServerState.changeServerAction is AsyncData.Failure }
            assertThat(failureState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()

            // Submit will return an error while validating the account provider
            initialState.eventSink(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))

            // Check an error was returned
            val submittedState = awaitState { it.changeServerState.changeServerAction is AsyncData.Failure }

            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitState { it.changeServerState.changeServerAction is AsyncData.Uninitialized }
            assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - confirm account creation without oidc generates an error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            // Check an error was returned
            val submittedState = awaitLoginMode { it is AsyncData.Failure }
            assertThat(submittedState.loginModeState.loginMode.errorOrNull()).isInstanceOf(AccountCreationNotSupported::class.java)
            // Assert the error is then cleared
            submittedState.eventSink(ConfirmAccountProviderEvents.ClearError)
            val clearedState = awaitLoginMode { it is AsyncData.Uninitialized }
            assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - confirm account creation with OAuth is successful`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            params = ConfirmAccountProviderPresenter.Params(isAccountCreation = true),
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            val submittedState = awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedState.loginModeState.loginMode.dataOrNull()).isInstanceOf(LoginMode.OAuth::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - offers an autocomplete suggestion from the account provider history`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://randomcommunity.org"),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviderSuggestion).isNull()
            initialState.eventSink(ConfirmAccountProviderEvents.UserInputChanged("https://random"))
            val suggestionState = awaitState { it.accountProviderInput == "https://random" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo("https://randomcommunity.org")
        }
    }

    @Test
    fun `present - continue accepts the autocomplete suggestion rather than the typed prefix`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
            appPreferencesStore = InMemoryAppPreferencesStore(
                homeserverHistory = listOf("https://randomcommunity.org"),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.UserInputChanged("https://random"))
            val suggestionState = awaitState { it.accountProviderSuggestion != null }
            suggestionState.eventSink(ConfirmAccountProviderEvents.Continue("https://randomcommunity.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo("https://randomcommunity.org")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - offers matrix_org as an autocomplete suggestion even without any history`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = emptyList()),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.UserInputChanged("https://matr"))
            val suggestionState = awaitState { it.accountProviderInput == "https://matr" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
        }
    }

    @Test
    fun `present - offers matrix_org without the scheme as an autocomplete suggestion`() = runTest {
        val presenter = createConfirmAccountProviderPresenter(
            appPreferencesStore = InMemoryAppPreferencesStore(homeserverHistory = emptyList()),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.UserInputChanged("matr"))
            val suggestionState = awaitState { it.accountProviderInput == "matr" }
            assertThat(suggestionState.accountProviderSuggestion).isEqualTo("matrix.org")
        }
    }

    @Test
    fun `present - continue prepends https to a bare account provider`() = runTest {
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue("matrix.org"))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls.first()).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - continue configures the homeserver only once`() = runTest {
        // The consolidated Continue flow validates the account provider and then signs in. Login reuses the
        // details resolved during validation, so setHomeserver runs exactly once rather than once per phase.
        val submittedUrls = mutableListOf<String>()
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { url ->
                submittedUrls.add(url)
                Result.success(aMatrixHomeServerDetails(supportsOAuthLogin = true))
            },
        )
        val presenter = createConfirmAccountProviderPresenter(
            matrixAuthenticationService = authenticationService,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ConfirmAccountProviderEvents.Continue(AuthenticationConfig.MATRIX_ORG_URL))
            awaitLoginMode { it is AsyncData.Success }
            assertThat(submittedUrls).containsExactly(AuthenticationConfig.MATRIX_ORG_URL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Awaits until the emitted state's login mode matches [predicate], skipping the intermediate
     * account-provider validation states, and returns that state.
     */
    private suspend fun ReceiveTurbine<ConfirmAccountProviderState>.awaitLoginMode(
        predicate: (AsyncData<LoginMode>) -> Boolean,
    ): ConfirmAccountProviderState = awaitState { predicate(it.loginModeState.loginMode) }

    private suspend fun ReceiveTurbine<ConfirmAccountProviderState>.awaitState(
        predicate: (ConfirmAccountProviderState) -> Boolean,
    ): ConfirmAccountProviderState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    private fun createConfirmAccountProviderPresenter(
        params: ConfirmAccountProviderPresenter.Params = ConfirmAccountProviderPresenter.Params(isAccountCreation = false),
        accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        defaultOAuthActionFlow: OAuthActionFlow = FakeOAuthActionFlow(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    ) = ConfirmAccountProviderPresenter(
        params = params,
        accountProviderDataSource = accountProviderDataSource,
        appPreferencesStore = appPreferencesStore,
        loginModePresenter = createLoginModePresenter(
            authenticationService = matrixAuthenticationService,
            oAuthActionFlow = defaultOAuthActionFlow,
        ),
        changeServerPresenter = ChangeServerPresenter(
            authenticationService = matrixAuthenticationService,
            accountProviderDataSource = accountProviderDataSource,
            defaultAccountProviderAccessControl = DefaultAccountProviderAccessControl(
                enterpriseService = FakeEnterpriseService(
                    isAllowedToConnectToHomeserverResult = { true },
                    isElementProEnforcedResult = { false },
                ),
                isEnterpriseBuild = { false },
            ),
            localNetworkPermissionGate = LocalNetworkPermissionGate(
                advisor = FakeLocalNetworkPermissionAdvisor(),
                permissionsPresenterFactory = FakePermissionsPresenterFactory(FakePermissionsPresenter()),
            ),
        ),
    )
}
