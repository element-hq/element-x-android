/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.auth.FakeOidcRedirectUrlProvider
import io.element.android.libraries.oidc.api.OidcAction
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultOidcIntentResolverTest {
    @Test
    fun `test resolve oidc go back`() {
        val sut = createDefaultOidcIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(OidcAction.GoBack())
    }

    @Test
    fun `test resolve oidc success`() {
        val sut = createDefaultOidcIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            OidcAction.Success(
                url = "io.element.android:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
            )
        )
    }

    @Test
    fun `test resolve oidc invalid`() {
        val sut = createDefaultOidcIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/invalid".toUri()
        }
        assertThrows(IllegalStateException::class.java) {
            sut.resolve(intent)
        }
    }

    private fun createDefaultOidcIntentResolver(): DefaultOidcIntentResolver {
        return DefaultOidcIntentResolver(
            oidcUrlParser = DefaultOidcUrlParser(
                oidcRedirectUrlProvider = FakeOidcRedirectUrlProvider(),
            ),
        )
    }
}
