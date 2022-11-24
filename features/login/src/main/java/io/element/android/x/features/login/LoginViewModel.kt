package io.element.android.x.features.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel(initialState: LoginViewState) :
    MavericksViewModel<LoginViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()
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
                matrix.login(state.formState.login.trim(), state.formState.password.trim())
                matrix.activeClient().startSync()
            }.execute {
                copy(isLoggedIn = it)
            }
        }
    }

    fun onSetPassword(password: String) {
        formState.value = formState.value.copy(password = password)
        setState { copy(isLoggedIn = Uninitialized) }
    }

    fun onSetName(name: String) {
        formState.value = formState.value.copy(login = name)
        setState { copy(isLoggedIn = Uninitialized) }
    }
}