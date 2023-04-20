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

package io.element.android.features.login.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.core.SessionId

open class LoginRootStateProvider : PreviewParameterProvider<LoginRootState> {
    override val values: Sequence<LoginRootState>
        get() = sequenceOf(
            aLoginRootState(),
            aLoginRootState().copy(homeserverDetails = MatrixHomeServerDetails("some-custom-server.com", supportsPasswordLogin = true, supportsOidc = false)),
            aLoginRootState().copy(formState = LoginFormState("user", "pass")),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.LoggingIn),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.ErrorLoggingIn(Throwable())),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.LoggedIn(SessionId("@user:domain"))),
            // Oidc
            aLoginRootState().copy(homeserverDetails = MatrixHomeServerDetails("server-with-oidc.org", supportsPasswordLogin = false, supportsOidc = true)),
            // No password, no oidc support
            aLoginRootState().copy(homeserverDetails = MatrixHomeServerDetails("wrong.org", supportsPasswordLogin = false, supportsOidc = false)),
        )
}

fun aLoginRootState() = LoginRootState(
    homeserverDetails = MatrixHomeServerDetails("matrix.org", supportsPasswordLogin = true, supportsOidc = false),
    loggedInState = LoggedInState.NotLoggedIn,
    formState = LoginFormState.Default,
    eventSink = {}
)
