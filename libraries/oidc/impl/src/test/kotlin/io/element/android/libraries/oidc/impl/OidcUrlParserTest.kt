/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.OidcConfig
import io.element.android.libraries.oidc.api.OidcAction
import org.junit.Assert
import org.junit.Test

class OidcUrlParserTest {
    @Test
    fun `test empty url`() {
        val sut = OidcUrlParser()
        assertThat(sut.parse("")).isNull()
    }

    @Test
    fun `test regular url`() {
        val sut = OidcUrlParser()
        assertThat(sut.parse("https://matrix.org")).isNull()
    }

    @Test
    fun `test cancel url`() {
        val sut = OidcUrlParser()
        val aCancelUrl = OidcConfig.REDIRECT_URI + "?error=access_denied&state=IFF1UETGye2ZA8pO"
        assertThat(sut.parse(aCancelUrl)).isEqualTo(OidcAction.GoBack)
    }

    @Test
    fun `test success url`() {
        val sut = OidcUrlParser()
        val aSuccessUrl = OidcConfig.REDIRECT_URI + "?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
        assertThat(sut.parse(aSuccessUrl)).isEqualTo(OidcAction.Success(aSuccessUrl))
    }

    @Test
    fun `test unknown url`() {
        val sut = OidcUrlParser()
        val anUnknownUrl = OidcConfig.REDIRECT_URI + "?state=IFF1UETGye2ZA8pO&goat=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
        Assert.assertThrows(IllegalStateException::class.java) {
            assertThat(sut.parse(anUnknownUrl))
        }
    }
}
