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

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.junit.Test
import org.matrix.rustcomponents.sdk.AuthenticationException as RustAuthenticationException

class AuthenticationExceptionMappingTest {
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
        assertThat(mappedException).isException<AuthenticationException.Generic>("Generic exception")
    }

    @Test
    fun `mapping specific exceptions map to their kotlin counterparts`() {
        assertThat(RustAuthenticationException.ClientMissing("Client missing").mapAuthenticationException())
            .isException<AuthenticationException.ClientMissing>("Client missing")

        assertThat(RustAuthenticationException.Generic("Generic").mapAuthenticationException()).isException<AuthenticationException.Generic>("Generic")

        assertThat(RustAuthenticationException.InvalidServerName("Invalid server name").mapAuthenticationException())
            .isException<AuthenticationException.InvalidServerName>("Invalid server name")

        assertThat(RustAuthenticationException.SessionMissing("Session missing").mapAuthenticationException())
            .isException<AuthenticationException.SessionMissing>("Session missing")

        assertThat(RustAuthenticationException.SlidingSyncNotAvailable("Sliding sync not available").mapAuthenticationException())
            .isException<AuthenticationException.SlidingSyncNotAvailable>("Sliding sync not available")
    }

    @Test
    fun `mapping Oidc related exceptions creates an 'OidcError' with different types`() {
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcException("Oidc exception"),
            type = "OidcException",
            message = "Oidc exception"
        )
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcMetadataInvalid("Oidc metadata invalid"),
            type = "OidcMetadataInvalid",
            message = "Oidc metadata invalid"
        )
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcMetadataMissing("Oidc metadata missing"),
            type = "OidcMetadataMissing",
            message = "Oidc metadata missing"
        )
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcNotSupported("Oidc not supported"),
            type = "OidcNotSupported",
            message = "Oidc not supported"
        )
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcCancelled("Oidc cancelled"),
            type = "OidcCancelled",
            message = "Oidc cancelled"
        )
        assertIsOidcError(
            throwable = RustAuthenticationException.OidcCallbackUrlInvalid("Oidc callback url invalid"),
            type = "OidcCallbackUrlInvalid",
            message = "Oidc callback url invalid"
        )
    }

    private inline fun <reified T> ThrowableSubject.isException(message: String) {
        isInstanceOf(T::class.java)
        hasMessageThat().isEqualTo(message)
    }

    private fun assertIsOidcError(throwable: Throwable, type: String, message: String) {
        val authenticationException = throwable.mapAuthenticationException()
        assertThat(authenticationException).isInstanceOf(AuthenticationException.OidcError::class.java)
        assertThat((authenticationException as? AuthenticationException.OidcError)?.type).isEqualTo(type)
        assertThat(authenticationException.message).isEqualTo(message)
    }
}
