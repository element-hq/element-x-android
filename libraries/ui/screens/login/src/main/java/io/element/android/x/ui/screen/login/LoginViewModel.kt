package io.element.android.x.ui.screen.login

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

    init {
        observeState()
    }

    private fun observeState() {
        onEach(
            LoginViewState::homeserver,
            LoginViewState::login,
            LoginViewState::password,
            LoginViewState::isLoggedIn,
        ) { homeserver, login, password, isLoggedIn ->
            setState {
                copy(
                    submitEnabled = homeserver.isNotEmpty() &&
                            login.isNotEmpty() &&
                            password.isNotEmpty() &&
                            isLoggedIn !is Loading
                )
            }
        }
    }

    fun handle(action: LoginActions) {
        when (action) {
            is LoginActions.SetHomeserver -> handleSetHomeserver(action)
            is LoginActions.SetLogin -> handleSetName(action)
            is LoginActions.SetPassword -> handleSetPassword(action)
            LoginActions.Submit -> handleSubmit()
        }
    }

    private fun handleSetHomeserver(action: LoginActions.SetHomeserver) {
        setState {
            copy(
                homeserver = action.homeserver
            )
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

    private fun handleSetPassword(action: LoginActions.SetPassword) {
        setState {
            copy(
                password = action.password
            )
        }
    }

    private fun handleSetName(action: LoginActions.SetLogin) {
        setState {
            copy(

                login = action.login
            )
        }
    }
}