package io.element.android.x.features.login

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class LoginViewState(
    val homeserver: String = "",
    val isLoggedIn: Async<Unit> = Uninitialized,
    val formState: LoginFormState = LoginFormState.Default,
) : MavericksState {
    val submitEnabled =
        formState.login.isNotEmpty() && formState.password.isNotEmpty() && isLoggedIn !is Loading
}

data class LoginFormState(
    val login: String,
    val password: String
) {

    companion object {
        val Default = LoginFormState("", "")
    }
}
