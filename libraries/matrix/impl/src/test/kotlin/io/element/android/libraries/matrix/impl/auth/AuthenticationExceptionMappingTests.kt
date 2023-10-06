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

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.junit.Test
import org.matrix.rustcomponents.sdk.AuthenticationException as RustAuthenticationException

class AuthenticationExceptionMappingTests {

    @Test
    fun `mapping an exception with no message returns 'Unknown error' message`() {
        val exception = Exception()
        val mappedException = exception.mapAuthenticationException()
        assertThat(mappedException.message).isEqualTo("Unknown error")
    }

    @Test
    fun `mapping a generic exception returns a Generic AuthenticationException`() {
        val exception = Exception("Generic exception")
        val mappedException = exception.mapAuthenticationException()
        assertThat(mappedException).isEqualTo(AuthenticationException.Generic("Generic exception"))
    }

    @Test
    fun `mapping specific exceptions map to their kotlin counterparts`() {
        assertThat(RustAuthenticationException.ClientMissing("Client missing").mapAuthenticationException())
                .isEqualTo(AuthenticationException.ClientMissing("Client missing"))

        assertThat(RustAuthenticationException.Generic("Generic").mapAuthenticationException()).isEqualTo(AuthenticationException.Generic("Generic"))

        assertThat(RustAuthenticationException.InvalidServerName("Invalid server name").mapAuthenticationException())
                .isEqualTo(AuthenticationException.InvalidServerName("Invalid server name"))

        assertThat(RustAuthenticationException.SessionMissing("Session missing").mapAuthenticationException())
            .isEqualTo(AuthenticationException.SessionMissing("Session missing"))

        assertThat(RustAuthenticationException.SlidingSyncNotAvailable("Sliding sync not available").mapAuthenticationException())
            .isEqualTo(AuthenticationException.SlidingSyncNotAvailable("Sliding sync not available"))
    }

    @Test
    fun `mapping Oidc related exceptions creates an 'OidcError' with different types`() {
        assertThat(RustAuthenticationException.OidcException("Oidc exception").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcException", "Oidc exception"))

        assertThat(RustAuthenticationException.OidcMetadataInvalid("Oidc metadata invalid").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcMetadataInvalid", "Oidc metadata invalid"))

        assertThat(RustAuthenticationException.OidcMetadataMissing("Oidc metadata missing").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcMetadataMissing", "Oidc metadata missing"))

        assertThat(RustAuthenticationException.OidcNotSupported("Oidc not supported").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcNotSupported", "Oidc not supported"))

        assertThat(RustAuthenticationException.OidcCancelled("Oidc cancelled").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcCancelled", "Oidc cancelled"))

        assertThat(RustAuthenticationException.OidcCallbackUrlInvalid("Oidc callback url invalid").mapAuthenticationException())
            .isEqualTo(AuthenticationException.OidcError("OidcCallbackUrlInvalid", "Oidc callback url invalid"))
    }

}
