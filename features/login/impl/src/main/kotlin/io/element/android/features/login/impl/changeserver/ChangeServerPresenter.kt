/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeServerPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val enterpriseService: EnterpriseService,
) : Presenter<ChangeServerState> {
    @Composable
    override fun present(): ChangeServerState {
        val localCoroutineScope = rememberCoroutineScope()

        val changeServerAction: MutableState<AsyncData<Unit>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        fun handleEvents(event: ChangeServerEvents) {
            when (event) {
                is ChangeServerEvents.ChangeServer -> localCoroutineScope.changeServer(event.accountProvider, changeServerAction)
                ChangeServerEvents.ClearError -> changeServerAction.value = AsyncData.Uninitialized
            }
        }

        return ChangeServerState(
            changeServerAction = changeServerAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.changeServer(
        data: AccountProvider,
        changeServerAction: MutableState<AsyncData<Unit>>,
    ) = launch {
        suspend {
            if (enterpriseService.isAllowedToConnectToHomeserver(data.url).not()) {
                throw UnauthorizedAccountProviderException(
                    unauthorisedAccountProviderTitle = data.title,
                    authorisedAccountProviderTitles = enterpriseService.defaultHomeserverList(),
                )
            }
            authenticationService.setHomeserver(data.url).map {
                authenticationService.getHomeserverDetails().value!!
                // Valid, remember user choice
                accountProviderDataSource.userSelection(data)
            }.getOrThrow()
        }.runCatchingUpdatingState(changeServerAction, errorTransform = ChangeServerError::from)
    }
}
