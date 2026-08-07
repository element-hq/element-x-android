/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.features.login.impl.localnetwork.LocalNetworkPermissionGate
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService

@Inject
class ChangeServerPresenter(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val defaultAccountProviderAccessControl: DefaultAccountProviderAccessControl,
    private val localNetworkPermissionGate: LocalNetworkPermissionGate,
) : Presenter<ChangeServerState> {
    @Composable
    override fun present(): ChangeServerState {
        val changeServerAction: MutableState<AsyncData<Unit>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        val gateState = localNetworkPermissionGate.present<AccountProvider>(
            urlOf = { provider -> provider.url },
            onProceed = { provider -> changeServer(provider, changeServerAction) },
        )

        fun handleEvent(event: ChangeServerEvents) {
            when (event) {
                ChangeServerEvents.ClearError -> changeServerAction.value = AsyncData.Uninitialized
                is ChangeServerEvents.ChangeServer -> gateState.submit(event.accountProvider)
                ChangeServerEvents.DismissLocalNetworkPermission -> gateState.abort()
                ChangeServerEvents.RequestLocalNetworkPermission -> gateState.requestPermission()
            }
        }

        return ChangeServerState(
            changeServerAction = changeServerAction.value,
            localNetworkPermissionDialog = gateState.dialog,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun changeServer(
        data: AccountProvider,
        changeServerAction: MutableState<AsyncData<Unit>>,
    ) {
        suspend {
            defaultAccountProviderAccessControl.assertIsAllowedToConnectToAccountProvider(
                title = data.title,
                accountProviderUrl = data.url,
            )
            val details = authenticationService.setHomeserver(data.url).getOrThrow()
            if (!details.isSupported) {
                throw ChangeServerError.UnsupportedServer
            }
            // Homeserver is valid, remember user choice
            accountProviderDataSource.setAccountProvider(data)
        }.runCatchingUpdatingState(changeServerAction, errorTransform = ChangeServerError::from)
    }
}
