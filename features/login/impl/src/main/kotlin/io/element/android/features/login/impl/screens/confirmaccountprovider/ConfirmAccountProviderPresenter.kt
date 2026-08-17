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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

@AssistedInject
class ConfirmAccountProviderPresenter(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val loginModePresenter: Presenter<LoginModeState>,
    private val changeServerPresenter: Presenter<ChangeServerState>,
    private val appPreferencesStore: AppPreferencesStore,
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
        val homeserverHistory by appPreferencesStore.getHomeserverHistoryFlow().collectAsState(emptyList())

        // Editable input, seeded from the current (history-defaulted) account provider until the user edits it.
        var userInput by rememberSaveable { mutableStateOf<String?>(null) }
        val accountProviderInput = userInput ?: accountProvider.url

        // Offer an account provider that the current input is a prefix of: the most recent previously-used
        // one first, then matrix.org. matrix.org is always available for autocomplete even before any
        // sign-in, offered both with and without the https:// scheme so it completes whether the user starts
        // typing "matrix.org" or "https://matrix.org".
        val accountProviderSuggestion = remember(accountProviderInput, homeserverHistory) {
            val input = accountProviderInput.trim()
            val candidates = homeserverHistory +
                AuthenticationConfig.MATRIX_ORG_URL +
                AuthenticationConfig.MATRIX_ORG_URL.removePrefix("https://")
            input.takeIf { it.isNotEmpty() }
                ?.let { prefix ->
                    candidates.firstOrNull { it.length > prefix.length && it.startsWith(prefix, ignoreCase = true) }
                }
        }

        // The account provider submitted via Continue (carried by the event, so it reflects the exact field
        // text at click time); used to actually sign in once validation succeeds.
        var submittedAccountProvider by remember { mutableStateOf<String?>(null) }
        val latestSubmittedAccountProvider by rememberUpdatedState(submittedAccountProvider)

        // Once the chosen account provider has been validated and persisted, proceed with the actual sign in / sign up.
        // Reuse the details resolved while validating, so login does not configure (and re-network) the homeserver again.
        LaunchedEffect(changeServerState.changeServerAction) {
            val homeServerDetails = changeServerState.changeServerAction.dataOrNull()
            if (homeServerDetails != null) {
                loginModeState.eventSink(
                    LoginModeEvent.Submit(
                        isAccountCreation = params.isAccountCreation,
                        homeserverUrl = latestSubmittedAccountProvider.orEmpty().trim(),
                        resolvedHomeserverUrl = null,
                        loginHint = null,
                        preConfiguredDetails = homeServerDetails,
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
                // account-provider access control, which the login submit does not run on its own.
                is ConfirmAccountProviderEvents.Continue -> {
                    // Default the scheme to https:// so entering a bare host (e.g. "matrix.org") works.
                    val accountProviderUrl = event.accountProvider.trim().ensureProtocol()
                    submittedAccountProvider = accountProviderUrl
                    changeServerState.eventSink(
                        ChangeServerEvents.ChangeServer(AccountProvider(url = accountProviderUrl))
                    )
                }
                ConfirmAccountProviderEvents.ClearError -> {
                    loginModeState.eventSink(LoginModeEvent.ClearError)
                    changeServerState.eventSink(ChangeServerEvents.ClearError)
                }
            }
        }

        return ConfirmAccountProviderState(
            accountProviderInput = accountProviderInput,
            accountProviderSuggestion = accountProviderSuggestion,
            isAccountCreation = params.isAccountCreation,
            loginModeState = loginModeState,
            changeServerState = changeServerState,
            eventSink = ::handleEvent,
        )
    }
}
