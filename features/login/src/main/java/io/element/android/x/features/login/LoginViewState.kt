package io.element.android.x.features.login

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class LoginViewState(
    val homeserver: String = "",
    val login: String = "",
    val password: String = "",
    val isLoggedIn: Async<Unit> = Uninitialized,
) : MavericksState {
    val submitEnabled = login.isNotEmpty() && password.isNotEmpty() && isLoggedIn !is Loading
}
