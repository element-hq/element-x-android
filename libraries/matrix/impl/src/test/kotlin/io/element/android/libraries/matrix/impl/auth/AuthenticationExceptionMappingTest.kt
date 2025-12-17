/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import org.junit.Test
import org.matrix.rustcomponents.sdk.ClientBuildException
import org.matrix.rustcomponents.sdk.OidcException

class AuthenticationExceptionMappingTest {
    @Test
    fun `mapping an exception with no message returns null message`() {
        val exception = Exception()
        val mappedException = exception.mapAuthenticationException()
        assertThat(mappedException.message).isNull()
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
            .isException<AuthenticationException.ServerUnreachable>("Server unreachable")
        assertThat(ClientBuildException.SlidingSync("Sliding Sync").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("Sliding Sync")
        assertThat(ClientBuildException.WellKnownDeserializationException("WellKnown Deserialization").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("WellKnown Deserialization")
        assertThat(ClientBuildException.WellKnownLookupFailed("WellKnown Lookup Failed").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("WellKnown Lookup Failed")
        assertThat(ClientBuildException.EventCache("EventCache error").mapAuthenticationException())
            .isException<AuthenticationException.Generic>("EventCache error")
    }

    @Test
    fun `mapping Oidc exceptions map to the Oidc Kotlin`() {
        assertThat(OidcException.Generic("Generic").mapAuthenticationException())
            .isException<AuthenticationException.Oidc>("Generic")
        assertThat(OidcException.CallbackUrlInvalid("CallbackUrlInvalid").mapAuthenticationException())
            .isException<AuthenticationException.Oidc>("CallbackUrlInvalid")
        assertThat(OidcException.Cancelled("Cancelled").mapAuthenticationException())
            .isException<AuthenticationException.Oidc>("Cancelled")
        assertThat(OidcException.MetadataInvalid("MetadataInvalid").mapAuthenticationException())
            .isException<AuthenticationException.Oidc>("MetadataInvalid")
        assertThat(OidcException.NotSupported("NotSupported").mapAuthenticationException())
            .isException<AuthenticationException.Oidc>("NotSupported")
    }

    private inline fun <reified T> ThrowableSubject.isException(message: String) {
        isInstanceOf(T::class.java)
        hasMessageThat().isEqualTo(message)
    }
}
