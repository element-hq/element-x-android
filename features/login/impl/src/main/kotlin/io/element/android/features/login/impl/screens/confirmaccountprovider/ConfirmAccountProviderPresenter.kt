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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

class ConfirmAccountProviderPresenter @AssistedInject constructor(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val authenticationService: MatrixAuthenticationService
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

        val loginFlowAction: MutableState<Async<LoginFlow>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: ConfirmAccountProviderEvents) {
            when (event) {
                ConfirmAccountProviderEvents.Continue -> {
                    localCoroutineScope.submit(accountProvider.title, loginFlowAction)
                }
                ConfirmAccountProviderEvents.ClearError -> loginFlowAction.value = Async.Uninitialized
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
        loginFlowAction: MutableState<Async<LoginFlow>>,
    ) = launch {
        suspend {
            val domain = tryOrNull { URL(homeserverUrl) }?.host ?: homeserverUrl
            authenticationService.setHomeserver(domain).map {
                val matrixHomeServerDetails = authenticationService.getHomeserverDetails().value!!
                if (matrixHomeServerDetails.supportsOidcLogin) {
                    // Retrieve the details right now
                    LoginFlow.OidcFlow(authenticationService.getOidcUrl().getOrThrow())
                } else if (matrixHomeServerDetails.supportsPasswordLogin) {
                    LoginFlow.PasswordLogin
                } else {
                    throw IllegalStateException("Unsupported login flow")
                }
            }.getOrThrow()
        }.runCatchingUpdatingState(loginFlowAction, exceptionTransform = ChangeServerError::from)
    }
}
