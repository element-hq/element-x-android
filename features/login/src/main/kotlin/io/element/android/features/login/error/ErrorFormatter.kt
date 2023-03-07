/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.login.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.matrix.api.auth.AuthErrorCode
import io.element.android.libraries.matrix.api.auth.errorCode
import org.matrix.rustcomponents.sdk.AuthenticationException
import io.element.android.libraries.ui.strings.R.string as StringR

@Composable
fun loginError(
    throwable: Throwable
): String {
    val authException = throwable as? AuthenticationException ?: return stringResource(StringR.unknown_error)
    return when (authException.errorCode) {
        AuthErrorCode.FORBIDDEN -> stringResource(StringR.auth_invalid_login_param)
        AuthErrorCode.USER_DEACTIVATED -> stringResource(StringR.auth_invalid_login_deactivated_account)
        AuthErrorCode.UNKNOWN -> stringResource(StringR.unknown_error)
    }
}

@Composable
fun changeServerError(
    throwable: Throwable
): String {
    val authException = throwable as? AuthenticationException ?: return stringResource(StringR.unknown_error)
    return when (authException) {
        is AuthenticationException.InvalidServerName -> stringResource(StringR.login_error_homeserver_not_found)
        else -> stringResource(StringR.unknown_error)
    }
}
