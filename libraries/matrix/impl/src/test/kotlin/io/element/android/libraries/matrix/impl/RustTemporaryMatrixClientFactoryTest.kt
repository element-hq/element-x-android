/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.impl.auth.FakeProxyProvider
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import io.element.android.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustTemporaryMatrixClientFactoryTest {
    @Test
    fun `create returns a TemporaryMatrixClient`() = runTest {
        val sut = createRustTemporaryMatrixClientFactory()
        val result = sut.create("https://matrix.org")
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `create can fail gracefully`() = runTest {
        val sut = createRustTemporaryMatrixClientFactory(
            rustMatrixClientFactory = createRustMatrixClientFactory(
                clientBuilderProvider = FakeClientBuilderProvider(
                    provideResult = { throw IllegalStateException("Failed to create client builder") }
                )
            )
        )
        val result = sut.create("https://matrix.org")
        assertThat(result.isFailure).isTrue()
    }

    private fun TestScope.createRustTemporaryMatrixClientFactory(
        sessionPathsFactory: SessionPathsFactory = SessionPathsFactory(File("/base"), File("/cache")),
        rustMatrixClientFactory: RustMatrixClientFactory = createRustMatrixClientFactory(),
    ): RustTemporaryMatrixClientFactory {
        return RustTemporaryMatrixClientFactory(
            sessionPathsFactory = sessionPathsFactory,
            rustMatrixClientFactory = rustMatrixClientFactory,
        )
    }

    private fun TestScope.createRustMatrixClientFactory(
        cacheDirectory: File = File("/cache"),
        sessionStore: SessionStore = InMemorySessionStore(
            updateUserProfileResult = { _, _, _ -> },
        ),
        clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
        workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
    ) = RustMatrixClientFactory(
        cacheDirectory = cacheDirectory,
        appCoroutineScope = backgroundScope,
        coroutineDispatchers = testCoroutineDispatchers(),
        sessionStore = sessionStore,
        userAgentProvider = SimpleUserAgentProvider(),
        proxyProvider = FakeProxyProvider(),
        clock = FakeSystemClock(),
        analyticsService = FakeAnalyticsService(),
        featureFlagService = FakeFeatureFlagService(),
        timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
        clientBuilderProvider = clientBuilderProvider,
        sqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
        workManagerScheduler = workManagerScheduler,
        clientBuilderEnterpriseHook = { builder, _ -> builder },
    )
}
