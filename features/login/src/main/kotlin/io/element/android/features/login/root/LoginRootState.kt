/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.login.root

import android.os.Parcelable
import io.element.android.libraries.matrix.core.SessionId
import kotlinx.parcelize.Parcelize

data class LoginRootState(
    val homeserver: String,
    val loggedInState: LoggedInState,
    val formState: LoginFormState,
    val eventSink: (LoginRootEvents) -> Unit
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

fun aLoginRootState() = LoginRootState(
    homeserver = "",
    loggedInState = LoggedInState.NotLoggedIn,
    formState = LoginFormState.Default,
    eventSink = {}
)
