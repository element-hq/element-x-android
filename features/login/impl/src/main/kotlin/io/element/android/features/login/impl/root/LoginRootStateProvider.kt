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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.core.SessionId

open class LoginRootStateProvider : PreviewParameterProvider<LoginRootState> {
    override val values: Sequence<LoginRootState>
        get() = sequenceOf(
            aLoginRootState(),
            aLoginRootState().copy(
                homeserverDetails = Async.Success(
                    MatrixHomeServerDetails(
                        "some-custom-server.com",
                        supportsPasswordLogin = true,
                        supportsOidcLogin = false
                    )
                )
            ),
            aLoginRootState().copy(formState = LoginFormState("user", "pass")),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.LoggingIn),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.ErrorLoggingIn(Throwable())),
            aLoginRootState().copy(formState = LoginFormState("user", "pass"), loggedInState = LoggedInState.LoggedIn(SessionId("@user:domain"))),
            // Oidc
            aLoginRootState().copy(
                homeserverUrl = "server-with-oidc.org",
                homeserverDetails = Async.Success(
                    MatrixHomeServerDetails(
                        "server-with-oidc.org",
                        supportsPasswordLogin = false,
                        supportsOidcLogin = true
                    )
                )
            ),
            // No password, no oidc support
            aLoginRootState().copy(
                homeserverUrl = "wrong.org",
                homeserverDetails = Async.Success(
                    MatrixHomeServerDetails(
                        "wrong.org",
                        supportsPasswordLogin = false,
                        supportsOidcLogin = false
                    )
                )
            ),
            // Loading
            aLoginRootState().copy(homeserverDetails = Async.Loading()),
            //Error
            aLoginRootState().copy(homeserverDetails = Async.Failure(Exception("An error occurred"))),
        )
}

fun aLoginRootState() = LoginRootState(
    homeserverUrl = "matrix.org",
    homeserverDetails = Async.Success(MatrixHomeServerDetails("matrix.org", supportsPasswordLogin = true, supportsOidcLogin = false)),
    loggedInState = LoggedInState.NotLoggedIn,
    formState = LoginFormState.Default,
    eventSink = {}
)
