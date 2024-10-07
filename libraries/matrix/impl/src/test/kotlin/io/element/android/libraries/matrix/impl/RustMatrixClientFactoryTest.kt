/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.auth.FakeProxyProvider
import io.element.android.libraries.matrix.impl.auth.FakeUserCertificatesProvider
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventTypeFilterFactory
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustMatrixClientFactoryTest {
    @Test
    fun test() = runTest {
        val sut = createRustMatrixClientFactory()
        val result = sut.create(aSessionData())
        assertThat(result.sessionId).isEqualTo(SessionId("@alice:server.org"))
        result.close()
    }
}

fun TestScope.createRustMatrixClientFactory(
    baseDirectory: File = File("/base"),
    cacheDirectory: File = File("/cache"),
    sessionStore: SessionStore = InMemorySessionStore(),
) = RustMatrixClientFactory(
    baseDirectory = baseDirectory,
    cacheDirectory = cacheDirectory,
    appCoroutineScope = this,
    coroutineDispatchers = testCoroutineDispatchers(),
    sessionStore = sessionStore,
    userAgentProvider = SimpleUserAgentProvider(),
    userCertificatesProvider = FakeUserCertificatesProvider(),
    proxyProvider = FakeProxyProvider(),
    clock = FakeSystemClock(),
    analyticsService = FakeAnalyticsService(),
    featureFlagService = FakeFeatureFlagService(),
    timelineEventTypeFilterFactory = FakeTimelineEventTypeFilterFactory(),
    clientBuilderProvider = FakeClientBuilderProvider(),
)
