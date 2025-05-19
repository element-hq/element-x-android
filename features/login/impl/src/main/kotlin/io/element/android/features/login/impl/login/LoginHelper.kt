/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.confirmaccountprovider.LoginFlow
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.features.login.impl.web.WebClientUrlForAuthenticationRetriever
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.OidcPrompt
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginHelper @Inject constructor(
    private val oidcActionFlow: OidcActionFlow,
    private val authenticationService: MatrixAuthenticationService,
    private val defaultLoginUserStory: DefaultLoginUserStory,
    private val webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever,
) {
    private lateinit var loginFlowAction: MutableState<AsyncData<LoginFlow>>

    val loginFlow: AsyncData<LoginFlow>
        get() = loginFlowAction.value

    @Composable
    fun Start() {
        loginFlowAction = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        LaunchedEffect(Unit) {
            oidcActionFlow.collect { oidcAction ->
                if (oidcAction != null) {
                    onOidcAction(oidcAction)
                }
            }
        }
    }

    fun clearError() {
        loginFlowAction.value = AsyncData.Uninitialized
    }

    fun submit(
        coroutineScope: CoroutineScope,
        isAccountCreation: Boolean,
        homeserverUrl: String,
        loginHint: String?,
    ) = coroutineScope.launch {
        suspend {
            authenticationService.setHomeserver(homeserverUrl).map {
                val matrixHomeServerDetails = authenticationService.getHomeserverDetails().value!!
                if (matrixHomeServerDetails.supportsOidcLogin) {
                    // Retrieve the details right now
                    val oidcPrompt = if (isAccountCreation) OidcPrompt.Create else OidcPrompt.Login
                    LoginFlow.OidcFlow(
                        authenticationService.getOidcUrl(prompt = oidcPrompt, loginHint = loginHint).getOrThrow()
                    )
                } else if (isAccountCreation) {
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

    private suspend fun onOidcAction(oidcAction: OidcAction) {
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
