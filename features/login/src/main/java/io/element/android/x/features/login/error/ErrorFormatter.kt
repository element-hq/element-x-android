package io.element.android.x.features.login.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.x.core.uri.isValidUrl
import io.element.android.x.features.login.root.LoginFormState
import io.element.android.x.element.resources.R as ElementR

@Composable
fun loginError(
    data: LoginFormState,
    throwable: Throwable?
): String {
    return when {
        data.login.isEmpty() -> "Please enter a login"
        data.password.isEmpty() -> "Please enter a password"
        throwable != null -> stringResource(id = ElementR.string.auth_invalid_login_param)
        else -> "No error provided"
    }
}

@Composable
fun changeServerError(
    data: String,
    throwable: Throwable?
): String {
    return when {
        data.isEmpty() -> "Please enter a server URL"
        !data.isValidUrl() -> stringResource(id = ElementR.string.login_error_invalid_home_server)
        throwable != null -> "That server doesn’t seem right. Please check the address."
        else -> "No error provided"
    }
}
