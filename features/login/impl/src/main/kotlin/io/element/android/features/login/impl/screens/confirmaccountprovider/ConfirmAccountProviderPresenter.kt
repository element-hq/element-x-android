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
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.di.AuthScope
import io.element.android.features.login.impl.login.AuthenticationHelper
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderPresenter.Params
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch

@AssistedInject
class ConfirmAccountProviderPresenter(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val authenticationHelper: AuthenticationHelper,
) : Presenter<ConfirmAccountProviderState> {
    data class Params(
        val isAccountCreation: Boolean,
    )

    @AssistedFactory
    @ContributesBinding(AuthScope::class)
    fun interface Factory : ConfirmAccountProviderPresenterFactory {
        override fun create(params: Params): ConfirmAccountProviderPresenter
    }

    @Composable
    override fun present(): ConfirmAccountProviderState {
        val accountProvider by accountProviderDataSource.flow.collectAsState()
        val localCoroutineScope = rememberCoroutineScope()

        val loginMode by authenticationHelper.collectAuthenticationMode()

        fun handleEvent(event: ConfirmAccountProviderEvents) {
            when (event) {
                ConfirmAccountProviderEvents.Continue -> localCoroutineScope.launch {
                    authenticationHelper.submit(
                        isAccountCreation = params.isAccountCreation,
                        homeserverUrl = accountProvider.url,
                        loginHint = null,
                    )
                }
                ConfirmAccountProviderEvents.ClearError -> authenticationHelper.clearError()
            }
        }

        return ConfirmAccountProviderState(
            accountProvider = accountProvider,
            isAccountCreation = params.isAccountCreation,
            authenticationMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}

interface ConfirmAccountProviderPresenterFactory {
    fun create(params: Params): ConfirmAccountProviderPresenter
}
