/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.uri.ensureProtocol
import javax.inject.Inject

class ChooseAccountProviderPresenter @Inject constructor(
    private val enterpriseService: EnterpriseService,
    private val loginHelper: LoginHelper,
) : Presenter<ChooseAccountProviderState> {
    @Composable
    override fun present(): ChooseAccountProviderState {
        val localCoroutineScope = rememberCoroutineScope()
        val loginMode by loginHelper.collectLoginMode()

        var selectedAccountProvider: AccountProvider? by remember { mutableStateOf(null) }

        fun handleEvent(event: ChooseAccountProviderEvents) {
            when (event) {
                ChooseAccountProviderEvents.Continue -> {
                    selectedAccountProvider?.let {
                        loginHelper.submit(
                            coroutineScope = localCoroutineScope,
                            isAccountCreation = false,
                            homeserverUrl = it.url,
                            loginHint = null,
                        )
                    }
                }
                is ChooseAccountProviderEvents.SelectAccountProvider -> {
                    // Ensure that the user do not change the server during processing
                    if (loginMode is AsyncData.Uninitialized) {
                        selectedAccountProvider = event.accountProvider
                    }
                }
                ChooseAccountProviderEvents.ClearError -> loginHelper.clearError()
            }
        }

        val staticAccountProviderList = remember {
            // The list cannot contains ANY_ACCOUNT_PROVIDER ("*") and cannot be empty at this point
            enterpriseService.defaultHomeserverList()
                .map { it.ensureProtocol() }
                .map { url ->
                    AccountProvider(
                        url = url,
                        subtitle = null,
                        isPublic = url == AuthenticationConfig.MATRIX_ORG_URL,
                        isMatrixOrg = url == AuthenticationConfig.MATRIX_ORG_URL,
                        isValid = true,
                    )
                }
        }

        return ChooseAccountProviderState(
            accountProviders = staticAccountProviderList,
            selectedAccountProvider = selectedAccountProvider,
            loginMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}
