package io.element.android.x.features.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.launch

class LoginViewModel(initialState: LoginViewState) :
    MavericksViewModel<LoginViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

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
                if (matrix.getHomeserver() == null) {
                    matrix.setHomeserver(state.homeserver)
                }
                matrix.login(state.login.trim(), state.password.trim())
                matrix.activeClient().startSync()
            }.execute {
                copy(isLoggedIn = it)
            }
        }
    }

    fun onSetPassword(password: String) {
        setState {
            copy(
                password = password,
                isLoggedIn = Uninitialized,
            )
        }
    }

    fun onSetName(name: String) {
        setState {
            copy(
                login = name,
                isLoggedIn = Uninitialized,
            )
        }
    }
}