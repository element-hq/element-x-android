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
