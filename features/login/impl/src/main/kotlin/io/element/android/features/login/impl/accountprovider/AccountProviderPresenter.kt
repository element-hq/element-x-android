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

package io.element.android.features.login.impl.accountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.login.impl.changeserver.ChangeServerError
import io.element.android.features.login.impl.datasource.AccountProviderDataSource
import io.element.android.features.login.impl.util.LoginConstants
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL

data class AccountProviderPresenterParams(
    val isAccountCreation: Boolean,
)

class AccountProviderPresenter @AssistedInject constructor(
    @Assisted private val params: AccountProviderPresenterParams,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val authenticationService: MatrixAuthenticationService
) : Presenter<AccountProviderState> {

    @AssistedFactory
    interface Factory {
        fun create(params: AccountProviderPresenterParams): AccountProviderPresenter
    }

    @Composable
    override fun present(): AccountProviderState {
        val accountProvider by accountProviderDataSource.flow().collectAsState()
        val currentHomeServerDetails = authenticationService.getHomeserverDetails().collectAsState().value
        val getHomeServerDetailsAction: MutableState<Async<MatrixHomeServerDetails>> = remember {
            if (currentHomeServerDetails != null) {
                mutableStateOf(Async.Success(currentHomeServerDetails))
            } else {
                mutableStateOf(Async.Uninitialized)
            }
        }
        val localCoroutineScope = rememberCoroutineScope()

        val homeserver = rememberSaveable {
            mutableStateOf(currentHomeServerDetails?.url ?: LoginConstants.DEFAULT_HOMESERVER_URL)
        }
        val loginFlowAction: MutableState<Async<LoginFlow>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: AccountProviderEvents) {
            when (event) {
                AccountProviderEvents.Continue -> {
                    localCoroutineScope.submit(homeserver, loginFlowAction)
                }
                AccountProviderEvents.ClearError -> loginFlowAction.value = Async.Uninitialized
            }
        }

        return AccountProviderState(
            homeserver = accountProvider.title,
            isMatrix = accountProvider.isMatrixOrg,
            isAccountCreation = params.isAccountCreation,
            loginFlow = loginFlowAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(
        homeserverUrl: MutableState<String>,
        loginFlowAction: MutableState<Async<LoginFlow>>,
    ) = launch {
        suspend {
            val domain = tryOrNull { URL(homeserverUrl.value) }?.host ?: homeserverUrl.value
            homeserverUrl.value = domain
            authenticationService.setHomeserver(domain).map {
                authenticationService.getHomeserverDetails().value!!
            }.map {
                if (it.supportsOidcLogin) {
                    // Retrieve the details right now
                    LoginFlow.OidcFlow(authenticationService.getOidcUrl().getOrThrow())
                } else if (it.supportsPasswordLogin) {
                    LoginFlow.PasswordLogin
                } else {
                    throw IllegalStateException("Unsupported login flow")
                }
            }.getOrThrow()
        }.execute(loginFlowAction, errorMapping = ChangeServerError::from)
    }
}
