package io.element.android.x.features.login

sealed interface LoginActions {
    data class SetHomeserver(val homeserver: String) : LoginActions
    data class SetLogin(val login: String) : LoginActions
    data class SetPassword(val password: String) : LoginActions
    object Submit : LoginActions
}
