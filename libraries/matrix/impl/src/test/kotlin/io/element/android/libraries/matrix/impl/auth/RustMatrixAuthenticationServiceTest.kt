/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.createRustMatrixClientFactory
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.libraries.matrix.test.auth.FakeOidcRedirectUrlProvider
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustMatrixAuthenticationServiceTest {
    @Test
    fun `getLatestSessionId should return the value from the store`() = runTest {
        val sessionStore = InMemorySessionStore()
        val sut = createRustMatrixAuthenticationService(
            sessionStore = sessionStore,
        )
        assertThat(sut.getLatestSessionId()).isNull()
        sessionStore.storeData(aSessionData(sessionId = "@alice:server.org"))
        assertThat(sut.getLatestSessionId()).isEqualTo(SessionId("@alice:server.org"))
    }

    private fun TestScope.createRustMatrixAuthenticationService(
        sessionStore: SessionStore = InMemorySessionStore(),
    ): RustMatrixAuthenticationService {
        val baseDirectory = File("/base")
        val cacheDirectory = File("/cache")
        val rustMatrixClientFactory = createRustMatrixClientFactory(
            baseDirectory = baseDirectory,
            cacheDirectory = cacheDirectory,
            sessionStore = sessionStore,
        )
        return RustMatrixAuthenticationService(
            sessionPathsFactory = SessionPathsFactory(baseDirectory, cacheDirectory),
            coroutineDispatchers = testCoroutineDispatchers(),
            sessionStore = sessionStore,
            rustMatrixClientFactory = rustMatrixClientFactory,
            passphraseGenerator = FakePassphraseGenerator(),
            oidcConfigurationProvider = OidcConfigurationProvider(
                buildMeta = aBuildMeta(),
                oidcRedirectUrlProvider = FakeOidcRedirectUrlProvider(),
            ),
        )
    }
}
