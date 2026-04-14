/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.loginwithclassic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.classic.ElementClassicConnection
import io.element.android.features.login.impl.classic.ElementClassicConnectionState
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.launch

@AssistedInject
class LoginWithClassicPresenter(
    @Assisted private val userId: UserId,
    @Assisted private val navigator: LoginWithClassicNavigator,
    private val loginHelper: LoginHelper,
    private val elementClassicConnection: ElementClassicConnection,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val buildMeta: BuildMeta,
) : Presenter<LoginWithClassicState> {
    @AssistedFactory
    interface Factory {
        fun create(
            userId: UserId,
            navigator: LoginWithClassicNavigator,
        ): LoginWithClassicPresenter
    }

    @Composable
    override fun present(): LoginWithClassicState {
        val coroutineScope = rememberCoroutineScope()
        var loginWithClassicAction by remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val loginMode by loginHelper.collectLoginMode()
        val elementClassicConnectionState by elementClassicConnection.stateFlow.collectAsState()

        fun handleEvent(event: LoginWithClassicEvent) {
            when (event) {
                LoginWithClassicEvent.Submit -> {
                    val currentState = elementClassicConnection.stateFlow.value
                    if (currentState is ElementClassicConnectionState.ElementClassicReady) {
                        if (currentState.elementClassicSession.secrets != null &&
                            !currentState.elementClassicSession.doesContainBackupKey) {
                            navigator.navigateToMissingKeyBackup()
                        } else {
                            coroutineScope.launch {
                                loginWithClassicAction = AsyncAction.Loading
                                // Ensure that the current account provider is set
                                val elementClassicUserId = currentState.elementClassicSession.userId
                                val accountProvider = elementClassicUserId.domainName.orEmpty().ensureProtocol()
                                accountProviderDataSource.setUrl(accountProvider)
                                loginHelper.submit(
                                    isAccountCreation = false,
                                    homeserverUrl = accountProvider,
                                    resolvedHomeserverUrl = currentState.elementClassicSession.homeserverUrl,
                                    loginHint = "mxid:" + elementClassicUserId.value,
                                )
                            }
                        }
                    } else {
                        loginWithClassicAction = AsyncAction.Failure(IllegalStateException("Element Classic is not ready"))
                    }
                }
                LoginWithClassicEvent.ClearError -> {
                    loginWithClassicAction = AsyncAction.Uninitialized
                    loginHelper.clearError()
                }
            }
        }

        val elementClassicReady = elementClassicConnectionState as? ElementClassicConnectionState.ElementClassicReady
        return LoginWithClassicState(
            isElementPro = buildMeta.isEnterpriseBuild,
            userId = userId,
            displayName = elementClassicReady?.displayName,
            avatar = elementClassicReady?.avatar,
            loginMode = loginMode,
            loginWithClassicAction = loginWithClassicAction,
            eventSink = ::handleEvent,
        )
    }
}
