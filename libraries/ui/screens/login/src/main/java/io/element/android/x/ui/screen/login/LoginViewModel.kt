package io.element.android.x.ui.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.element.android.x.sdk.matrix.MatrixInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val matrix = MatrixInstance.getInstance()

    private val _state = MutableStateFlow(LoginViewState())
    val state = _state.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        // TODO Update submitEnabled when other state members are updated.
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
        _state.value = _state.value.copy(
            homeserver = action.homeserver,
            submitEnabled = _state.value.login.isNotEmpty() &&
                    _state.value.password.isNotEmpty() &&
                    action.homeserver.isNotEmpty()
        )
    }

    private fun handleSubmit() {
        viewModelScope.launch {
            val currentState = state.value
            try {
                matrix.login(currentState.homeserver, currentState.login, currentState.password)
            } catch (throwable: Throwable) {
                Log.e("Error", "Cannot login", throwable)
            }
        }
    }

    private fun handleSetPassword(action: LoginActions.SetPassword) {
        _state.value = _state.value.copy(
            password = action.password,
            submitEnabled = _state.value.login.isNotEmpty() &&
                    _state.value.homeserver.isNotEmpty() &&
                    action.password.isNotEmpty()
        )
    }

    private fun handleSetName(action: LoginActions.SetLogin) {
        _state.value = _state.value.copy(
            login = action.login,
            submitEnabled = action.login.isNotEmpty() &&
                    _state.value.homeserver.isNotEmpty() &&
                    _state.value.password.isNotEmpty()
        )
    }
}