/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch

@AssistedInject
class ConfirmAccountProviderPresenter(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val loginHelper: LoginHelper,
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
        val accountProvider by accountProviderDataSource.flow.collectAsState()
        val localCoroutineScope = rememberCoroutineScope()

        val loginMode by loginHelper.collectLoginMode()

        fun handleEvent(event: ConfirmAccountProviderEvents) {
            when (event) {
                ConfirmAccountProviderEvents.Continue -> localCoroutineScope.launch {
                    loginHelper.submit(
                        isAccountCreation = params.isAccountCreation,
                        homeserverUrl = accountProvider.url,
                        loginHint = null,
                    )
                }
                ConfirmAccountProviderEvents.ClearError -> loginHelper.clearError()
            }
        }

        return ConfirmAccountProviderState(
            accountProvider = accountProvider,
            isAccountCreation = params.isAccountCreation,
            loginMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}
