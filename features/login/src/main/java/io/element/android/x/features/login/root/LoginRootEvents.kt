package io.element.android.x.features.login.root

sealed interface LoginRootEvents {
    object RefreshHomeServer : LoginRootEvents
    data class SetLogin(val login: String) : LoginRootEvents
    data class SetPassword(val password: String) : LoginRootEvents
    object Submit : LoginRootEvents
}
