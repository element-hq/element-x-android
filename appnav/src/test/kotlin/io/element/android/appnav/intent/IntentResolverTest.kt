/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.intent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.LoginParams
import io.element.android.features.login.test.FakeLoginIntentResolver
import io.element.android.libraries.deeplink.api.DeeplinkData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.test.FakeOidcIntentResolver
import io.element.android.tests.testutils.lambda.lambdaError
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
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Root(A_SESSION_ID)
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
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
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
                eventId = null,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = null,
                    eventId = null,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent thread`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
                eventId = null,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = A_THREAD_ID,
                    eventId = null,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent event`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
                eventId = AN_EVENT_ID,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = null,
                    eventId = AN_EVENT_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent thread and event`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
                eventId = AN_EVENT_ID,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = A_THREAD_ID,
                    eventId = AN_EVENT_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve oidc`() {
        val sut = createIntentResolver(
            oidcIntentResolverResult = { OidcAction.GoBack() },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element.android:/?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Oidc(
                oidcAction = OidcAction.GoBack()
            )
        )
    }

    @Test
    fun `test resolve external permalink`() {
        val permalinkData = PermalinkData.UserLink(
            userId = UserId("@alice:matrix.org")
        )
        val sut = createIntentResolver(
            loginIntentResolverResult = { null },
            permalinkParserResult = { permalinkData },
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "https://matrix.to/#/@alice:matrix.org".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Permalink(
                permalinkData = permalinkData
            )
        )
    }

    @Test
    fun `test resolve external permalink, FallbackLink should be ignored`() {
        val sut = createIntentResolver(
            permalinkParserResult = { PermalinkData.FallbackLink(Uri.parse("https://matrix.org")) },
            loginIntentResolverResult = { null },
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "https://matrix.to/#/@alice:matrix.org".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve external permalink, invalid action`() {
        val permalinkData = PermalinkData.UserLink(
            userId = UserId("@alice:matrix.org")
        )
        val sut = createIntentResolver(
            permalinkParserResult = { permalinkData },
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_BATTERY_LOW
            data = "https://matrix.to/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test incoming share simple`() {
        val sut = createIntentResolver(
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_SEND
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.IncomingShare(intent = intent))
    }

    @Test
    fun `test incoming share multiple`() {
        val sut = createIntentResolver(
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_SEND_MULTIPLE
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.IncomingShare(intent = intent))
    }

    @Test
    fun `test resolve invalid`() {
        val sut = createIntentResolver(
            permalinkParserResult = { PermalinkData.FallbackLink(Uri.parse("https://matrix.org")) },
            loginIntentResolverResult = { null },
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve login param`() {
        val aLoginParams = LoginParams("accountProvider", null)
        val sut = createIntentResolver(
            loginIntentResolverResult = { aLoginParams },
            oidcIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.Login(aLoginParams))
    }

    private fun createIntentResolver(
        deeplinkParserResult: DeeplinkData? = null,
        permalinkParserResult: (String) -> PermalinkData = { lambdaError() },
        loginIntentResolverResult: (String) -> LoginParams? = { lambdaError() },
        oidcIntentResolverResult: (Intent) -> OidcAction? = { lambdaError() },
    ): IntentResolver {
        return IntentResolver(
            deeplinkParser = { deeplinkParserResult },
            loginIntentResolver = FakeLoginIntentResolver(
                parseResult = loginIntentResolverResult,
            ),
            oidcIntentResolver = FakeOidcIntentResolver(
                resolveResult = oidcIntentResolverResult,
            ),
            permalinkParser = FakePermalinkParser(
                result = permalinkParserResult
            ),
        )
    }
}
