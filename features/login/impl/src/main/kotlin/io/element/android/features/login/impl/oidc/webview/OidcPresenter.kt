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

package io.element.android.features.login.impl.oidc.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.OidcDetails
import kotlinx.coroutines.launch

class OidcPresenter @AssistedInject constructor(
    @Assisted private val oidcDetails: OidcDetails,
    private val authenticationService: MatrixAuthenticationService,
) : Presenter<OidcState> {

    @AssistedFactory
    interface Factory {
        fun create(oidcDetails: OidcDetails): OidcPresenter
    }

    @Composable
    override fun present(): OidcState {
        var requestState: AsyncAction<Unit> by remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }
        val localCoroutineScope = rememberCoroutineScope()

        fun handleCancel() {
            requestState = AsyncAction.Loading
            localCoroutineScope.launch {
                authenticationService.cancelOidcLogin()
                    .fold(
                        onSuccess = {
                            // Then go back
                            requestState = AsyncAction.Success(Unit)
                        },
                        onFailure = {
                            requestState = AsyncAction.Failure(it)
                        }
                    )
            }
        }

        fun handleSuccess(url: String) {
            requestState = AsyncAction.Loading
            localCoroutineScope.launch {
                authenticationService.loginWithOidc(url)
                    .onFailure {
                        requestState = AsyncAction.Failure(it)
                    }
                // On success, the node tree will be updated, there is nothing to do
            }
        }

        fun handleAction(action: OidcAction) {
            when (action) {
                OidcAction.GoBack -> handleCancel()
                is OidcAction.Success -> handleSuccess(action.url)
            }
        }

        fun handleEvents(event: OidcEvents) {
            when (event) {
                OidcEvents.Cancel -> handleCancel()
                is OidcEvents.OidcActionEvent -> handleAction(event.oidcAction)
                OidcEvents.ClearError -> requestState = AsyncAction.Uninitialized
            }
        }

        return OidcState(
            oidcDetails = oidcDetails,
            requestState = requestState,
            eventSink = ::handleEvents
        )
    }
}
