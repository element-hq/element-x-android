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

package io.element.android.appnav.intent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.impl.oidc.DefaultOidcIntentResolver
import io.element.android.features.login.impl.oidc.OidcUrlParser
import io.element.android.libraries.deeplink.DeepLinkCreator
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.deeplink.DeeplinkParser
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class IntentResolverTest {
    @Test
    fun `resolve launcher intent should return null`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve navigation intent root`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = DeepLinkCreator().room(
                sessionId = A_SESSION_ID,
                roomId = null,
                threadId = null,
            )
                .toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Root(
                    sessionId = A_SESSION_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent room`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = DeepLinkCreator().room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
            )
                .toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = null,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent thread`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = DeepLinkCreator().room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            )
                .toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = A_THREAD_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve oidc go back`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/callback?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Oidc(
                oidcAction = OidcAction.GoBack
            )
        )
    }

    @Test
    fun `test resolve oidc success`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/callback?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Oidc(
                oidcAction = OidcAction.Success(
                    url = "io.element:/callback?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
                )
            )
        )
    }

    @Test
    fun `test resolve oidc invalid`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/callback/invalid".toUri()
        }
        assertThrows(IllegalStateException::class.java) {
            sut.resolve(intent)
        }
    }

    @Test
    fun `test resolve invalid`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    private fun createIntentResolver(): IntentResolver {
        return IntentResolver(
            deeplinkParser = DeeplinkParser(),
            oidcIntentResolver = DefaultOidcIntentResolver(
                oidcUrlParser = OidcUrlParser()
            ),
            permalinkParser = FakePermalinkParser(
                result = { PermalinkData.FallbackLink(Uri.parse("https://matrix.org")) }
            ),
        )
    }
}
