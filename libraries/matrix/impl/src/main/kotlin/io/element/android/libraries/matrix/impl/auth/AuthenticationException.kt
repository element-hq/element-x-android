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
import org.matrix.rustcomponents.sdk.AuthenticationException as RustAuthenticationException

fun Throwable.mapAuthenticationException(): Throwable {
    return when (this) {
        is RustAuthenticationException.ClientMissing -> AuthenticationException.ClientMissing(this.message!!)
        is RustAuthenticationException.Generic -> AuthenticationException.Generic(this.message!!)
        is RustAuthenticationException.InvalidServerName -> AuthenticationException.InvalidServerName(this.message!!)
        is RustAuthenticationException.SessionMissing -> AuthenticationException.SessionMissing(this.message!!)
        is RustAuthenticationException.SlidingSyncNotAvailable -> AuthenticationException.SlidingSyncNotAvailable(this.message!!)

        is RustAuthenticationException.OidcException -> AuthenticationException.OidcError("OidcException", message!!)
        is RustAuthenticationException.OidcMetadataInvalid -> AuthenticationException.OidcError("OidcMetadataInvalid", message!!)
        is RustAuthenticationException.OidcMetadataMissing -> AuthenticationException.OidcError("OidcMetadataMissing", message!!)
        is RustAuthenticationException.OidcNotStarted -> AuthenticationException.OidcError("OidcNotStarted", message!!)
        is RustAuthenticationException.OidcNotSupported -> AuthenticationException.OidcError("OidcNotSupported", message!!)

        else -> this
    }
}
