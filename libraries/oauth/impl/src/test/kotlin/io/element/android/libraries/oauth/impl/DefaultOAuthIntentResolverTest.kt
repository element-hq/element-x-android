/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.impl

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import io.element.android.libraries.oauth.api.OAuthAction
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultOAuthIntentResolverTest {
    @Test
    fun `test resolve OAuth go back`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(OAuthAction.GoBack())
    }

    @Test
    fun `test resolve OAuth success`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            OAuthAction.Success(
                url = "io.element.android:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
            )
        )
    }

    @Test
    fun `test resolve OAuth invalid`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/invalid".toUri()
        }
        assertThrows(IllegalStateException::class.java) {
            sut.resolve(intent)
        }
    }

    private fun createDefaultOAuthIntentResolver(): DefaultOAuthIntentResolver {
        return DefaultOAuthIntentResolver(
            oAuthUrlParser = DefaultOAuthUrlParser(
                oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
            ),
        )
    }
}
