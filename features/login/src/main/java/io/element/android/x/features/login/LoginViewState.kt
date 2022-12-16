package io.element.android.x.features.login

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.matrix.MatrixClient

data class LoginViewState(
    val homeserver: String = "",
    val loggedInClient: Async<MatrixClient> = Uninitialized,
    val formState: LoginFormState = LoginFormState.Default,
) : MavericksState {
    val submitEnabled =
        formState.login.isNotEmpty() && formState.password.isNotEmpty() && loggedInClient !is Loading
}

data class LoginFormState(
    val login: String,
    val password: String
) {

    companion object {
        val Default = LoginFormState("", "")
    }

}