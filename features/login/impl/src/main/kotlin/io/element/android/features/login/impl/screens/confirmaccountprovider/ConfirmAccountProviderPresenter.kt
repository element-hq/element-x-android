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
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.oidc.customtab.DefaultOidcActionFlow
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConfirmAccountProviderPresenter @AssistedInject constructor(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val authenticationService: MatrixAuthenticationService,
    private val defaultOidcActionFlow: DefaultOidcActionFlow,
    private val defaultLoginUserStory: DefaultLoginUserStory,
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
            defaultOidcActionFlow.collect { oidcAction ->
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
                    LoginFlow.OidcFlow(authenticationService.getOidcUrl().getOrThrow())
                } else if (matrixHomeServerDetails.supportsPasswordLogin) {
                    LoginFlow.PasswordLogin
                } else {
                    error("Unsupported login flow")
                }
            }.getOrThrow()
        }.runCatchingUpdatingState(loginFlowAction, errorTransform = ChangeServerError::from)
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
        defaultOidcActionFlow.reset()
    }
}
