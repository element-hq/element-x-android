/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.login.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.login.impl.util.LoginConstants
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginRootPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
) : Presenter<LoginRootState> {

    @Composable
    override fun present(): LoginRootState {
        val localCoroutineScope = rememberCoroutineScope()
        val currentHomeServerDetails = authenticationService.getHomeserverDetails().collectAsState().value
        val homeserver = currentHomeServerDetails?.url ?: LoginConstants.DEFAULT_HOMESERVER_URL
        val getHomeServerDetailsAction: MutableState<Async<MatrixHomeServerDetails>> = remember {
            if (currentHomeServerDetails != null) {
                mutableStateOf(Async.Success(currentHomeServerDetails))
            } else {
                mutableStateOf(Async.Uninitialized)
            }
        }

        LaunchedEffect(Unit) {
            if (currentHomeServerDetails == null) {
                getHomeServerDetails(homeserver, getHomeServerDetailsAction)
            }
        }

        val loggedInState: MutableState<LoggedInState> = remember {
            mutableStateOf(LoggedInState.NotLoggedIn)
        }
        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }

        fun handleEvents(event: LoginRootEvents) {
            when (event) {
                LoginRootEvents.RetryFetchServerInfo -> localCoroutineScope.getHomeServerDetails(homeserver, getHomeServerDetailsAction)
                is LoginRootEvents.SetLogin -> updateFormState(formState) {
                    copy(login = event.login)
                }
                is LoginRootEvents.SetPassword -> updateFormState(formState) {
                    copy(password = event.password)
                }
                LoginRootEvents.Submit -> {
                    val homeServerDetails = getHomeServerDetailsAction.value.dataOrNull() ?: return
                    when {
                        homeServerDetails.supportsOidc -> localCoroutineScope.submitOidc(loggedInState)
                        homeServerDetails.supportsPasswordLogin -> localCoroutineScope.submit(formState.value, loggedInState)
                    }
                }
                LoginRootEvents.ClearError -> loggedInState.value = LoggedInState.NotLoggedIn
            }
        }

        return LoginRootState(
            homeserverUrl = homeserver,
            homeserverDetails = getHomeServerDetailsAction.value,
            loggedInState = loggedInState.value,
            formState = formState.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.getHomeServerDetails(
        homeserver: String,
        state: MutableState<Async<MatrixHomeServerDetails>>,
    ) = launch {
        state.value = Async.Loading()
        suspend {
            authenticationService.setHomeserver(homeserver)
            authenticationService.getHomeserverDetails().value!!
        }.execute(state)
    }

    private fun CoroutineScope.submitOidc(loggedInState: MutableState<LoggedInState>) = launch {
        loggedInState.value = LoggedInState.LoggingIn
        authenticationService.getOidcUrl()
            .onSuccess {
                loggedInState.value = LoggedInState.OidcStarted(it)
            }
            .onFailure { failure ->
                loggedInState.value = LoggedInState.ErrorLoggingIn(failure)
            }
    }

    private fun CoroutineScope.submit(formState: LoginFormState, loggedInState: MutableState<LoggedInState>) = launch {
        loggedInState.value = LoggedInState.LoggingIn
        authenticationService.login(formState.login.trim(), formState.password)
            .onSuccess { sessionId ->
                loggedInState.value = LoggedInState.LoggedIn(sessionId)
            }
            .onFailure { failure ->
                loggedInState.value = LoggedInState.ErrorLoggingIn(failure)
            }
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }
}
