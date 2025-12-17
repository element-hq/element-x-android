/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.auth.FAKE_REDIRECT_URL
import io.element.android.libraries.matrix.test.auth.FakeOidcRedirectUrlProvider
import io.element.android.libraries.oidc.api.OidcAction
import org.junit.Assert
import org.junit.Test

class DefaultOidcUrlParserTest {
    @Test
    fun `test empty url`() {
        val sut = createDefaultOidcUrlParser()
        assertThat(sut.parse("")).isNull()
    }

    @Test
    fun `test regular url`() {
        val sut = createDefaultOidcUrlParser()
        assertThat(sut.parse("https://matrix.org")).isNull()
    }

    @Test
    fun `test cancel url`() {
        val sut = createDefaultOidcUrlParser()
        val aCancelUrl = "$FAKE_REDIRECT_URL?error=access_denied&state=IFF1UETGye2ZA8pO"
        assertThat(sut.parse(aCancelUrl)).isEqualTo(OidcAction.GoBack())
    }

    @Test
    fun `test success url`() {
        val sut = createDefaultOidcUrlParser()
        val aSuccessUrl = "$FAKE_REDIRECT_URL?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
        assertThat(sut.parse(aSuccessUrl)).isEqualTo(OidcAction.Success(aSuccessUrl))
    }

    @Test
    fun `test unknown url`() {
        val sut = createDefaultOidcUrlParser()
        val anUnknownUrl = "$FAKE_REDIRECT_URL?state=IFF1UETGye2ZA8pO&goat=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
        Assert.assertThrows(IllegalStateException::class.java) {
            assertThat(sut.parse(anUnknownUrl))
        }
    }

    private fun createDefaultOidcUrlParser(): DefaultOidcUrlParser {
        return DefaultOidcUrlParser(
            oidcRedirectUrlProvider = FakeOidcRedirectUrlProvider(),
        )
    }
}
