/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.features.login.impl.web.WebClientUrlForAuthenticationRetriever
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.OidcPrompt
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConfirmAccountProviderPresenter @AssistedInject constructor(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val authenticationService: MatrixAuthenticationService,
    private val oidcActionFlow: OidcActionFlow,
    private val defaultLoginUserStory: DefaultLoginUserStory,
    private val webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever,
) : Presenter<ConfirmAccountProviderState> {
    data class Params(
        val isAccountCreation: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(params: Params): ConfirmAccountProviderPresenter
    }

    @Composable
    override fun present(): ConfirmAccountProviderState {
        val accountProvider by accountProviderDataSource.flow().collectAsState()
        val localCoroutineScope = rememberCoroutineScope()

        val loginFlowAction: MutableState<AsyncData<LoginFlow>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        LaunchedEffect(Unit) {
            oidcActionFlow.collect { oidcAction ->
                if (oidcAction != null) {
                    onOidcAction(oidcAction, loginFlowAction)
                }
            }
        }

        fun handleEvents(event: ConfirmAccountProviderEvents) {
            when (event) {
                ConfirmAccountProviderEvents.Continue -> {
                    localCoroutineScope.submit(accountProvider.url, loginFlowAction)
                }
                ConfirmAccountProviderEvents.ClearError -> loginFlowAction.value = AsyncData.Uninitialized
            }
        }

        return ConfirmAccountProviderState(
            accountProvider = accountProvider,
            isAccountCreation = params.isAccountCreation,
            loginFlow = loginFlowAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(
        homeserverUrl: String,
        loginFlowAction: MutableState<AsyncData<LoginFlow>>,
    ) = launch {
        suspend {
            authenticationService.setHomeserver(homeserverUrl).map {
                val matrixHomeServerDetails = authenticationService.getHomeserverDetails().value!!
                if (matrixHomeServerDetails.supportsOidcLogin) {
                    // Retrieve the details right now
                    val oidcPrompt = if (params.isAccountCreation) OidcPrompt.Create else OidcPrompt.Consent
                    LoginFlow.OidcFlow(authenticationService.getOidcUrl(oidcPrompt).getOrThrow())
                } else if (params.isAccountCreation) {
                    val url = webClientUrlForAuthenticationRetriever.retrieve(homeserverUrl)
                    LoginFlow.AccountCreationFlow(url)
                } else if (matrixHomeServerDetails.supportsPasswordLogin) {
                    LoginFlow.PasswordLogin
                } else {
                    error("Unsupported login flow")
                }
            }.getOrThrow()
        }.runCatchingUpdatingState(
            state = loginFlowAction,
            errorTransform = {
                when (it) {
                    is AccountCreationNotSupported -> it
                    else -> ChangeServerError.from(it)
                }
            }
        )
    }

    private suspend fun onOidcAction(
        oidcAction: OidcAction,
        loginFlowAction: MutableState<AsyncData<LoginFlow>>,
    ) {
        loginFlowAction.value = AsyncData.Loading()
        when (oidcAction) {
            OidcAction.GoBack -> {
                authenticationService.cancelOidcLogin()
                    .onSuccess {
                        loginFlowAction.value = AsyncData.Uninitialized
                    }
                    .onFailure { failure ->
                        loginFlowAction.value = AsyncData.Failure(failure)
                    }
            }
            is OidcAction.Success -> {
                authenticationService.loginWithOidc(oidcAction.url)
                    .onSuccess { _ ->
                        defaultLoginUserStory.setLoginFlowIsDone(true)
                    }
                    .onFailure { failure ->
                        loginFlowAction.value = AsyncData.Failure(failure)
                    }
            }
        }
        oidcActionFlow.reset()
    }
}
