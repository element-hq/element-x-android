package io.element.android.x.ui.screen.login

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized

data class LoginViewState(
    val homeserver: String = "matrix.org",
    val login: String = "",
    val password: String = "",
    val submitEnabled: Boolean = false,
    val isLoggedIn: Async<Unit> = Uninitialized,
) : MavericksState
