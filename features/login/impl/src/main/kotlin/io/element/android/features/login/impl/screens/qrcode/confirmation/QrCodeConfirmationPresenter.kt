/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.impl.oidc.customtab.DefaultOidcActionFlow
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import javax.inject.Inject

class QrCodeConfirmationPresenter @Inject constructor(
    private val oidcActionFlow: DefaultOidcActionFlow,
    private val authenticationService: MatrixAuthenticationService,
) : Presenter<QrCodeConfirmationState> {
    @Composable
    override fun present(): QrCodeConfirmationState {
        LaunchedEffect(Unit) {
            observeOidcAction()
        }
        return QrCodeConfirmationState(QrCodeConfirmationStep.DisplayCheckCode("12"))
    }

    private suspend fun observeOidcAction() {
        oidcActionFlow.collect {
            when (it) {
                null -> Unit
                is OidcAction.Success -> onOidcSuccess(it.url)
                is OidcAction.GoBack -> {

                }
            }
        }
    }

    private suspend fun onOidcSuccess(url: String) {
        authenticationService.loginWithOidc(url)
            .onSuccess { sessionId ->

            }
    }
}
