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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.screens.chooseaccountprovider.ChooseAccountProviderPresenter
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderPresenter
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.features.login.impl.screens.onboarding.OnBoardingPresenter
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

/**
 * This class is responsible for managing the login flow, including handling OIDC actions and
 * submitting login requests.
 * It's an helper to avoid code duplication. It is used by [OnBoardingPresenter], [ConfirmAccountProviderPresenter]
 * and [ChooseAccountProviderPresenter].
 */
class LoginHelper @Inject constructor(
    private val oidcActionFlow: OidcActionFlow,
    private val authenticationService: MatrixAuthenticationService,
    private val defaultLoginUserStory: DefaultLoginUserStory,
    private val webClientUrlForAuthenticationRetriever: WebClientUrlForAuthenticationRetriever,
) {
    private val loginModeState: MutableState<AsyncData<LoginMode>> = mutableStateOf(AsyncData.Uninitialized)

    @Composable
    fun collectLoginMode(): State<AsyncData<LoginMode>> {
        LaunchedEffect(Unit) {
            oidcActionFlow.collect { oidcAction ->
                if (oidcAction != null) {
                    onOidcAction(oidcAction)
                }
            }
        }
        return loginModeState
    }

    fun clearError() {
        loginModeState.value = AsyncData.Uninitialized
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
                    LoginMode.Oidc(
                        authenticationService.getOidcUrl(prompt = oidcPrompt, loginHint = loginHint).getOrThrow()
                    )
                } else if (isAccountCreation) {
                    val url = webClientUrlForAuthenticationRetriever.retrieve(homeserverUrl)
                    LoginMode.AccountCreation(url)
                } else if (matrixHomeServerDetails.supportsPasswordLogin) {
                    LoginMode.PasswordLogin
                } else {
                    error("Unsupported login flow")
                }
            }.getOrThrow()
        }.runCatchingUpdatingState(
            state = loginModeState,
            errorTransform = {
                when (it) {
                    is AccountCreationNotSupported -> it
                    else -> ChangeServerError.from(it)
                }
            }
        )
    }

    private suspend fun onOidcAction(oidcAction: OidcAction) {
        loginModeState.value = AsyncData.Loading()
        when (oidcAction) {
            OidcAction.GoBack -> {
                authenticationService.cancelOidcLogin()
                    .onSuccess {
                        loginModeState.value = AsyncData.Uninitialized
                    }
                    .onFailure { failure ->
                        loginModeState.value = AsyncData.Failure(failure)
                    }
            }
            is OidcAction.Success -> {
                authenticationService.loginWithOidc(oidcAction.url)
                    .onSuccess { _ ->
                        defaultLoginUserStory.setLoginFlowIsDone(true)
                    }
                    .onFailure { failure ->
                        loginModeState.value = AsyncData.Failure(failure)
                    }
            }
        }
        oidcActionFlow.reset()
    }
}
