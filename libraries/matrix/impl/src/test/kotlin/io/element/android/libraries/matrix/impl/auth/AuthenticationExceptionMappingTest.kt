/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.junit.Test
import org.matrix.rustcomponents.sdk.ClientBuildException

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
        assertThat(ClientBuildException.Generic("Unknown error").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("Unknown error")

        assertThat(ClientBuildException.InvalidServerName("Invalid server name").mapAuthenticationException())
            .isException<AuthenticationException.InvalidServerName>("Invalid server name")

        assertThat(ClientBuildException.SlidingSyncVersion("Sliding sync not available").mapAuthenticationException())
            .isException<AuthenticationException.SlidingSyncVersion>("Sliding sync not available")
    }

    @Test
    fun `mapping other exceptions map to the Generic Kotlin`() {
        assertThat(ClientBuildException.Sdk("SDK issue").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("SDK issue")
        assertThat(ClientBuildException.ServerUnreachable("Server unreachable").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("Server unreachable")
        assertThat(ClientBuildException.SlidingSync("Sliding Sync").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("Sliding Sync")
        assertThat(ClientBuildException.WellKnownDeserializationException("WellKnown Deserialization").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("WellKnown Deserialization")
        assertThat(ClientBuildException.WellKnownLookupFailed("WellKnown Lookup Failed").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("WellKnown Lookup Failed")
    }

    private inline fun <reified T> ThrowableSubject.isException(message: String) {
        isInstanceOf(T::class.java)
        hasMessageThat().isEqualTo(message)
    }
}
