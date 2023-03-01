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

package io.element.android.features.login.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginRootPresenter @Inject constructor(private val authenticationService: MatrixAuthenticationService) : Presenter<LoginRootState> {

    @Composable
    override fun present(): LoginRootState {
        val localCoroutineScope = rememberCoroutineScope()
        val homeserver = rememberSaveable {
            mutableStateOf(authenticationService.getHomeserverOrDefault())
        }
        val loggedInState: MutableState<LoggedInState> = remember {
            mutableStateOf(LoggedInState.NotLoggedIn)
        }
        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }

        fun handleEvents(event: LoginRootEvents) {
            when (event) {
                LoginRootEvents.RefreshHomeServer -> refreshHomeServer(homeserver)
                is LoginRootEvents.SetLogin -> updateFormState(formState) {
                    copy(login = event.login)
                }
                is LoginRootEvents.SetPassword -> updateFormState(formState) {
                    copy(password = event.password)
                }
                LoginRootEvents.Submit -> localCoroutineScope.submit(homeserver.value, formState.value, loggedInState)
            }
        }

        return LoginRootState(
            homeserver = homeserver.value,
            loggedInState = loggedInState.value,
            formState = formState.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(homeserver: String, formState: LoginFormState, loggedInState: MutableState<LoggedInState>) = launch {
        loggedInState.value = LoggedInState.LoggingIn
        //TODO rework the setHomeserver flow
        tryOrNull {
            authenticationService.setHomeserver(homeserver)
        }
        try {
            val sessionId = authenticationService.login(formState.login.trim(), formState.password.trim())
            loggedInState.value = LoggedInState.LoggedIn(sessionId)
        } catch (failure: Throwable) {
            loggedInState.value = LoggedInState.ErrorLoggingIn(failure)
        }
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }

    private fun refreshHomeServer(homeserver: MutableState<String>) {
        homeserver.value = authenticationService.getHomeserverOrDefault()
    }
}
