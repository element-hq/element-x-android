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
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.changeserver.ChangeServerEvent
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.login.LoginModeEvent
import io.element.android.features.login.impl.login.LoginModeState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

@AssistedInject
class ConfirmAccountProviderPresenter(
    @Assisted private val params: Params,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val loginModePresenter: Presenter<LoginModeState>,
    private val changeServerPresenter: Presenter<ChangeServerState>,
    private val appPreferencesStore: AppPreferencesStore,
    private val enterpriseService: EnterpriseService,
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

        // The account providers offered for autocomplete: previously-used ones first, then the
        // enterprise/MDM-configured allow-list, then matrix.org (always available, even before any sign-in).
        // The "*" wildcard is a routing marker, not a real provider, so it is filtered out. Everything is
        // rendered without the https:// scheme (added back at connection time).
        val autocompleteCandidates = remember(homeserverHistory) {
            (homeserverHistory + enterpriseService.homeserverAllowList() + AuthenticationConfig.MATRIX_ORG_URL)
                .filter { it != EnterpriseService.ANY_ACCOUNT_PROVIDER }
                .map { it.withoutScheme() }
                .distinct()
        }

        // Editable input, seeded from the current (history-defaulted) account provider until the user edits it.
        // Displayed without the scheme, so the field shows e.g. "matrix.org" rather than "https://matrix.org".
        var userInput by rememberSaveable { mutableStateOf<String?>(null) }
        val accountProviderInput = userInput ?: accountProvider.url.withoutScheme()

        // Offer the first candidate that the current input is a (case-insensitive) prefix of.
        val accountProviderSuggestion = remember(accountProviderInput, autocompleteCandidates) {
            val input = accountProviderInput.trim()
            input.takeIf { it.isNotEmpty() }
                ?.let { prefix ->
                    autocompleteCandidates.firstOrNull { it.length > prefix.length && it.startsWith(prefix, ignoreCase = true) }
                }
        }

        // The account provider submitted via Continue (carried by the event, so it reflects the exact field
        // text at click time); used to actually sign in once validation succeeds.
        var submittedAccountProvider by remember { mutableStateOf<String?>(null) }
        val latestSubmittedAccountProvider by rememberUpdatedState(submittedAccountProvider)

        // A login hint (the full Matrix user ID) captured when the user enters one, so the authentication
        // server can pre-fill their identity. Null when a plain account provider was entered.
        var submittedLoginHint by remember { mutableStateOf<String?>(null) }
        val latestSubmittedLoginHint by rememberUpdatedState(submittedLoginHint)

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
                        loginHint = latestSubmittedLoginHint,
                        preConfiguredDetails = homeServerDetails,
                    )
                )
            }
        }

        fun handleEvent(event: ConfirmAccountProviderEvent) {
            when (event) {
                is ConfirmAccountProviderEvent.UserInputChanged -> {
                    userInput = event.accountProvider
                }
                // Validate (and persist) the chosen account provider before proceeding. This also enforces the
                // account-provider access control, which the login submit does not run on its own.
                is ConfirmAccountProviderEvent.Continue -> {
                    // Apply the accepted account provider (any accepted completion) back into the field so it
                    // renders the full server rather than the typed prefix, and survives the OAuth round-trip.
                    val accountProvider = event.accountProvider.trim()
                    userInput = accountProvider
                    // If the user entered a full Matrix user ID (e.g. "@alice:example.org"), sign in to its
                    // homeserver and pass the ID as a login hint so the authentication server can pre-fill it.
                    // Otherwise treat the input as the account provider host. Only the homeserver is ever
                    // persisted to history, never the user ID.
                    val isUserId = MatrixPatterns.isUserId(accountProvider)
                    // For a user ID the homeserver is the domain part (after the ':'), keeping any port.
                    val host = if (isUserId) accountProvider.substringAfter(":") else accountProvider
                    submittedLoginHint = "mxid:$accountProvider".takeIf { isUserId && !params.isAccountCreation }
                    // Default the scheme to https:// so entering a bare host (e.g. "matrix.org") works.
                    val accountProviderUrl = host.ensureProtocol()
                    submittedAccountProvider = accountProviderUrl
                    changeServerState.eventSink(
                        ChangeServerEvent.ChangeServer(AccountProvider(url = accountProviderUrl))
                    )
                }
                ConfirmAccountProviderEvent.ClearError -> {
                    loginModeState.eventSink(LoginModeEvent.ClearError)
                    changeServerState.eventSink(ChangeServerEvent.ClearError)
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

/**
 * The host part of an account provider URL, i.e. the value with any `https://` / `http://` scheme removed.
 * Returns the input unchanged when it has no scheme (e.g. `matrix.org`).
 */
private fun String.withoutScheme(): String = substringAfter("://")
