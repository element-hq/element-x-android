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
