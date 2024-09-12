/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.oidc.api.OidcAction
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
