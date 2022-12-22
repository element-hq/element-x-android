/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class LoginViewModel @AssistedInject constructor(
    private val matrix: Matrix,
    @Assisted initialState: LoginViewState) :
    MavericksViewModel<LoginViewState>(initialState) {

    companion object : MavericksViewModelFactory<LoginViewModel, LoginViewState> by daggerMavericksViewModelFactory()

    var formState = mutableStateOf(LoginFormState.Default)
        private set

    init {
        snapshotFlow { formState.value }
            .onEach {
                setState { copy(formState = it) }
            }.launchIn(viewModelScope)
    }

    fun onResume() {
        val currentHomeserver = matrix.getHomeserverOrDefault()
        setState {
            copy(
                homeserver = currentHomeserver
            )
        }
    }

    fun onSubmit() {
        viewModelScope.launch {
            suspend {
                val state = awaitState()
                // Ensure the server is provided to the Rust SDK
                matrix.setHomeserver(state.homeserver)
                matrix.login(state.formState.login.trim(), state.formState.password.trim()).also {
                    it.startSync()
                }
            }.execute {
                copy(loggedInClient = it)
            }
        }
    }

    fun onSetPassword(password: String) {
        formState.value = formState.value.copy(password = password)
        setState { copy(loggedInClient = Uninitialized) }
    }

    fun onSetName(name: String) {
        formState.value = formState.value.copy(login = name)
        setState { copy(loggedInClient = Uninitialized) }
    }
}
