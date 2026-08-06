/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter

@AssistedInject
class ConfirmAccountProviderPresenter(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val loginModePresenter: Presenter<LoginModeState>,
    private val changeServerPresenter: Presenter<ChangeServerState>,
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
        val loginModeState = loginModePresenter.present()
        val changeServerState = changeServerPresenter.present()

        // Editable input, seeded from the current (history-defaulted) account provider until the user edits it.
        var userInput by rememberSaveable { mutableStateOf<String?>(null) }
        val accountProviderInput = userInput ?: accountProvider.url
        val latestAccountProviderInput by rememberUpdatedState(accountProviderInput)

        // Once the chosen account provider has been validated and persisted, proceed with the actual sign in / sign up.
        LaunchedEffect(changeServerState.changeServerAction) {
            if (changeServerState.changeServerAction is AsyncData.Success) {
                loginModeState.eventSink(
                    LoginModeEvent.Submit(
                        isAccountCreation = params.isAccountCreation,
                        homeserverUrl = latestAccountProviderInput.trim(),
                        resolvedHomeserverUrl = null,
                        loginHint = null,
                    )
                )
            }
        }

        fun handleEvent(event: ConfirmAccountProviderEvents) {
            when (event) {
                is ConfirmAccountProviderEvents.UserInputChanged -> {
                    userInput = event.accountProvider
                }
                // Validate (and persist) the chosen account provider before proceeding. This also enforces the
                // enterprise account-provider access control, which the login submit does not do on its own.
                ConfirmAccountProviderEvents.Continue -> changeServerState.eventSink(
                    ChangeServerEvents.ChangeServer(AccountProvider(url = accountProviderInput.trim()))
                )
                ConfirmAccountProviderEvents.ClearError -> {
                    loginModeState.eventSink(LoginModeEvent.ClearError)
                    changeServerState.eventSink(ChangeServerEvents.ClearError)
                }
            }
        }

        return ConfirmAccountProviderState(
            accountProviderInput = accountProviderInput,
            isAccountCreation = params.isAccountCreation,
            loginModeState = loginModeState,
            changeServerState = changeServerState,
            eventSink = ::handleEvent,
        )
    }
}
