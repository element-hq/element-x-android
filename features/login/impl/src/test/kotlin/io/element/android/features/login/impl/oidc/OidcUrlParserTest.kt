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

package io.element.android.features.login.impl.oidc

import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.libraries.matrix.api.auth.OidcConfig
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
