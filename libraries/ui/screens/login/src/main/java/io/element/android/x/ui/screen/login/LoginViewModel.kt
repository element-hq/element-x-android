package io.element.android.x.ui.screen.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginViewState())
    val state = _state.asStateFlow()

    fun handle(action: LoginActions) {
        when (action) {
            is LoginActions.SetLogin -> handleSetName(action)
            is LoginActions.SetPassword -> handleSetPassword(action)
            LoginActions.Submit -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        // TODO
    }

    private fun handleSetPassword(action: LoginActions.SetPassword) {
        _state.value = _state.value.copy(
            password = action.password,
            submitEnabled = _state.value.login.isNotEmpty() && action.password.isNotEmpty()
        )
    }

    private fun handleSetName(action: LoginActions.SetLogin) {
        _state.value = _state.value.copy(
            login = action.login,
            submitEnabled = action.login.isNotEmpty() && _state.value.password.isNotEmpty()
        )
    }
}