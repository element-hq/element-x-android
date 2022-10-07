package io.element.android.x.ui.screen.login

data class LoginViewState(
    val login: String = "",
    val password: String = "",
    val submitEnabled: Boolean = false,
)
