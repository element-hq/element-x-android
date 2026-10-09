/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeClientBuilderEnterpriseHook
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.impl.auth.FakeProxyProvider
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import io.element.android.libraries.matrix.impl.search.RustSearchBackfillService
import io.element.android.libraries.matrix.impl.search.workmanager.SearchBackfillRequestBuilder
import io.element.android.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import io.element.android.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientDelegate
import java.io.File
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

class RustMatrixClientFactoryTest {
    @Test
    fun test() = runTest {
        val scheduleWorkersLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = scheduleWorkersLambda, cancelLambda = { _, _ -> })
        val sut = createRustMatrixClientFactory(workManagerScheduler = workManagerScheduler)

        val result = sut.create(aSessionData())

        assertThat(result.sessionId).isEqualTo(SessionId("@alice:server.org"))
        // 2 workers are scheduled: one for the search backfill and one for the vacuum
        scheduleWorkersLambda.assertions().isCalledExactly(2)
        result.destroy()
    }

    @Test
    fun `an auth error on a client only invalidates its own session`() = runTest {
        val aliceSessionId = "@alice:server.org"
        val bobSessionId = "@bob:server.org"
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = aliceSessionId, isTokenValid = true),
                aSessionData(sessionId = bobSessionId, isTokenValid = true),
            ),
            updateUserProfileResult = { _, _, _, _ -> },
        )
        val userIds = ArrayDeque(listOf(aliceSessionId, bobSessionId))
        val delegates = mutableMapOf<String, ClientDelegate?>()
        val sut = createRustMatrixClientFactory(
            sessionStore = sessionStore,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}, cancelLambda = { _, _ -> }),
            clientBuilderProvider = FakeClientBuilderProvider {
                val userId = userIds.removeFirst()
                FakeFfiClientBuilder(
                    buildResult = {
                        FakeFfiClient(
                            userId = userId,
                            withUtdHook = {},
                            setDelegateResult = { delegates[userId] = it },
                        )
                    }
                )
            },
        )
        val aliceClient = sut.create(sessionStore.getSession(aliceSessionId)!!)
        val bobClient = sut.create(sessionStore.getSession(bobSessionId)!!)

        delegates.getValue(aliceSessionId)!!.didReceiveAuthError(isSoftLogout = false)

        assertThat(sessionStore.getSession(aliceSessionId)!!.isTokenValid).isFalse()
        assertThat(sessionStore.getSession(bobSessionId)!!.isTokenValid).isTrue()
        aliceClient.destroy()
        bobClient.destroy()
    }
}

fun TestScope.createRustMatrixClientFactory(
    cacheDirectory: File = File("/cache"),
    sessionStore: SessionStore = InMemorySessionStore(
        updateUserProfileResult = { _, _, _, _ -> },
    ),
    clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
    workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(cancelLambda = { _, _ -> }),
    sqliteStoreBuilderProvider: SqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
) = RustMatrixClientFactory(
    cacheDirectory = cacheDirectory,
    appCoroutineScope = backgroundScope,
    coroutineDispatchers = testCoroutineDispatchers(),
    sessionStore = sessionStore,
    userAgentProvider = SimpleUserAgentProvider(),
    proxyProvider = FakeProxyProvider(),
    analyticsService = FakeAnalyticsService(),
    featureFlagService = FakeFeatureFlagService(),
    clientBuilderProvider = clientBuilderProvider,
    sqliteStoreBuilderProvider = sqliteStoreBuilderProvider,
    clientBuilderEnterpriseHook = FakeClientBuilderEnterpriseHook(),
    innerMatrixClientFactory = createRustMatrixClientInnerFactory(sessionStore, workManagerScheduler),
)

private fun TestScope.createRustMatrixClientInnerFactory(
    sessionStore: SessionStore,
    workManagerScheduler: FakeWorkManagerScheduler,
) = object : RustMatrixClient.Factory {
    override fun create(
        sessionPaths: SessionPaths,
        innerClient: Client,
        innerSyncService: ClientSyncService,
        baseCacheDirectory: File,
        contentScanner: ContentScanner?,
        isMessageSearchAvailable: Boolean,
        sessionDelegate: RustClientSessionDelegate,
    ) = RustMatrixClient(
        sessionPaths = sessionPaths,
        innerClient = innerClient,
        innerSyncService = innerSyncService,
        baseCacheDirectory = baseCacheDirectory,
        contentScanner = contentScanner,
        isMessageSearchAvailable = isMessageSearchAvailable,
        sessionDelegate = sessionDelegate,
        sessionStore = sessionStore,
        appCoroutineScope = backgroundScope,
        dispatchers = testCoroutineDispatchers(),
        clock = FakeSystemClock(),
        timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
        featureFlagService = FakeFeatureFlagService(),
        analyticsService = FakeAnalyticsService(),
        workManagerScheduler = workManagerScheduler,
        searchBackfillServiceFactory = object : RustSearchBackfillService.Factory {
            override fun create(innerClient: Client) = RustSearchBackfillService(
                innerClient = innerClient,
                searchBackfillRequestBuilderFactory = {
                    object : SearchBackfillRequestBuilder {
                        override suspend fun build(): Result<List<WorkManagerRequestWrapper>> = Result.success(emptyList())
                    }
                },
                workManagerScheduler = workManagerScheduler,
            )
        },
    )
}
