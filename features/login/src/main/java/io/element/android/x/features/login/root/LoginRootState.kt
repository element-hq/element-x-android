package io.element.android.x.features.login.root

import android.os.Parcelable
import io.element.android.x.matrix.core.SessionId
import kotlinx.parcelize.Parcelize

data class LoginRootState(
    val homeserver: String = "",
    val loggedInState: LoggedInState = LoggedInState.NotLoggedIn,
    val formState: LoginFormState = LoginFormState.Default,
) {
    val submitEnabled =
        formState.login.isNotEmpty() && formState.password.isNotEmpty() && loggedInState != LoggedInState.LoggingIn
}

sealed interface LoggedInState {
    object NotLoggedIn : LoggedInState
    object LoggingIn : LoggedInState
    data class ErrorLoggingIn(val failure: Throwable) : LoggedInState
    data class LoggedIn(val sessionId: SessionId) : LoggedInState
}

@Parcelize
data class LoginFormState(
    val login: String,
    val password: String
) : Parcelable {

    companion object {
        val Default = LoginFormState("", "")
    }
}
