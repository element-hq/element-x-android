package io.element.android.x.ui.screen.login

sealed interface LoginActions {
    data class SetLogin(val login: String) : LoginActions
    data class SetPassword(val password: String) : LoginActions
    object Submit : LoginActions
}
