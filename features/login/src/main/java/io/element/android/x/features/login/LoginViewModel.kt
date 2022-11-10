package io.element.android.x.features.login

import com.airbnb.mvrx.MavericksViewModel
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.launch

class LoginViewModel(initialState: LoginViewState) :
    MavericksViewModel<LoginViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

    fun handle(action: LoginActions) {
        when (action) {
            is LoginActions.SetHomeserver -> handleSetHomeserver(action)
            is LoginActions.SetLogin -> handleSetName(action)
            is LoginActions.SetPassword -> handleSetPassword(action)
            LoginActions.Submit -> handleSubmit()
        }
    }

    private fun handleSubmit() = withState { state ->
        viewModelScope.launch {
            suspend {
                matrix.login(state.homeserver, state.login, state.password)
                matrix.activeClient().startSync()
            }.execute {
                copy(isLoggedIn = it)
            }
        }
    }

    private fun handleSetHomeserver(action: LoginActions.SetHomeserver) {
        setState { copy(homeserver = action.homeserver) }
    }

    private fun handleSetPassword(action: LoginActions.SetPassword) {
        setState { copy(password = action.password) }
    }

    private fun handleSetName(action: LoginActions.SetLogin) {
        setState { copy(login = action.login) }
    }
}