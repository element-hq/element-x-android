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

package io.element.android.features.login.impl.error

import io.element.android.features.login.impl.R
import io.element.android.libraries.matrix.api.auth.AuthErrorCode
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import io.element.android.libraries.matrix.api.auth.errorCode
import io.element.android.libraries.ui.strings.R.string as StringR

fun loginError(
    throwable: Throwable
): Int {
    val authException = throwable as? AuthenticationException ?: return StringR.error_unknown
    return when (authException.errorCode) {
        AuthErrorCode.FORBIDDEN -> R.string.screen_login_error_invalid_credentials
        AuthErrorCode.USER_DEACTIVATED -> R.string.screen_login_error_deactivated_account
        AuthErrorCode.UNKNOWN -> StringR.error_unknown
    }
}
