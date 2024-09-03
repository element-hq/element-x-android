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

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.matrix.rustcomponents.sdk.ClientBuildException as RustAuthenticationException

fun Throwable.mapAuthenticationException(): AuthenticationException {
    val message = this.message ?: "Unknown error"
    return when (this) {
        is RustAuthenticationException.Generic -> AuthenticationException.Generic(message)
        is RustAuthenticationException.InvalidServerName -> AuthenticationException.InvalidServerName(message)
        is RustAuthenticationException.SlidingSyncVersion -> AuthenticationException.SlidingSyncVersion(message)
        else -> AuthenticationException.Generic(message)
    }
}
