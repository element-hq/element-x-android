package io.element.android.x.features.login

import android.util.Log
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.sdk.matrix.MatrixInstance
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
            setState { copy(isLoggedIn = Loading()) }
            try {
                matrix.login(state.homeserver, state.login, state.password)
                setState { copy(isLoggedIn = Success(Unit)) }
            } catch (throwable: Throwable) {
                Log.e("Error", "Cannot login", throwable)
                setState { copy(isLoggedIn = Fail(throwable)) }
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