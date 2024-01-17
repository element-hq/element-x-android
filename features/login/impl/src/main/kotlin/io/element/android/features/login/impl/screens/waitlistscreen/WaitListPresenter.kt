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

package io.element.android.features.login.impl.screens.waitlistscreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.screens.loginpassword.LoginFormState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class WaitListPresenter @AssistedInject constructor(
    @Assisted private val formState: LoginFormState,
    private val buildMeta: BuildMeta,
    private val authenticationService: MatrixAuthenticationService,
    private val defaultLoginUserStory: DefaultLoginUserStory,
) : Presenter<WaitListState> {
    @AssistedFactory
    interface Factory {
        fun create(loginFormState: LoginFormState): WaitListPresenter
    }

    @Composable
    override fun present(): WaitListState {
        val coroutineScope = rememberCoroutineScope()
        val homeserverUrl = remember {
            authenticationService.getHomeserverDetails().value?.url ?: "server"
        }

        val loginAction: MutableState<AsyncData<SessionId>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        val attemptNumber = remember { mutableIntStateOf(0) }

        fun handleEvents(event: WaitListEvents) {
            when (event) {
                WaitListEvents.AttemptLogin -> {
                    // Do not attempt to login on first resume of the View.
                    attemptNumber.intValue++
                    if (attemptNumber.intValue > 1) {
                        coroutineScope.loginAttempt(formState, loginAction)
                    }
                }
                WaitListEvents.ClearError -> loginAction.value = AsyncData.Uninitialized
                WaitListEvents.Continue -> defaultLoginUserStory.setLoginFlowIsDone(true)
            }
        }

        return WaitListState(
            appName = buildMeta.applicationName,
            serverName = homeserverUrl,
            loginAction = loginAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.loginAttempt(formState: LoginFormState, loggedInState: MutableState<AsyncData<SessionId>>) = launch {
        Timber.w("Attempt to login...")
        loggedInState.value = AsyncData.Loading()
        authenticationService.login(formState.login.trim(), formState.password)
            .onSuccess { sessionId ->
                loggedInState.value = AsyncData.Success(sessionId)
            }
            .onFailure { failure ->
                loggedInState.value = AsyncData.Failure(failure)
            }
    }
}
