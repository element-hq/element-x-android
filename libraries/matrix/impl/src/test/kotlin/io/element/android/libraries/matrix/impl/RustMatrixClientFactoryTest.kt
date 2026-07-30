/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.scanner.ContentScannerUrlProvider
import io.element.android.libraries.matrix.impl.auth.FakeProxyProvider
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import io.element.android.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import io.element.android.libraries.network.useragent.SimpleUserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RustMatrixClientFactoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    @Test
    fun test() = runTest {
        val scheduleVacuumLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = scheduleVacuumLambda)
        val sut = createRustMatrixClientFactory(workManagerScheduler = workManagerScheduler)

        val result = sut.create(aSessionData())

        assertThat(result.sessionId).isEqualTo(SessionId("@alice:server.org"))
        scheduleVacuumLambda.assertions().isCalledOnce()
        result.destroy()
    }

    @Test
    fun `create - message search is unavailable when the client secret is missing`() = runTest {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(FeatureFlags.MessageSearch.key to true),
        )
        val sut = createRustMatrixClientFactory(
            featureFlagService = featureFlagService,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )

        val result = sut.create(aSessionData().copy(passphrase = null))

        assertThat(result.isMessageSearchAvailable).isFalse()
        result.destroy()
    }

    @Test
    fun `create - message search availability uses the client builder decision`() = runTest {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(FeatureFlags.MessageSearch.key to true),
        )
        val sut = createRustMatrixClientFactory(
            featureFlagService = featureFlagService,
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )

        val result = sut.create(
            aSessionData(
                sessionPath = temporaryFolder.newFolder("session").absolutePath,
                cachePath = temporaryFolder.newFolder("cache").absolutePath,
            ).copy(passphrase = "aSecret")
        )

        assertThat(result.isMessageSearchAvailable).isTrue()
        featureFlagService.setFeatureEnabled(FeatureFlags.MessageSearch, false)
        assertThat(result.isMessageSearchAvailable).isTrue()
        result.destroy()
    }

    @Test
    fun `create - deletes the event cache store when the search index does not cover it`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        val storeFiles = listOf(
            EVENT_CACHE_STORE_NAME,
            "$EVENT_CACHE_STORE_NAME-wal",
            "$EVENT_CACHE_STORE_NAME-shm",
        ).map { File(cacheFolder, it).apply { createNewFile() } }
        var buildCount = 0
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
            clientBuilderProvider = FakeClientBuilderProvider {
                FakeFfiClientBuilder {
                    buildCount++
                    // The default clearCachesResult fails the test if the FFI clear is ever used:
                    // it needs a running sync service to expire the sliding-sync position, and
                    // discarding the then-unstable client aborts in a native finalizer.
                    FakeFfiClient(withUtdHook = {})
                }
            },
        )

        val result = sut.create(
            aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret")
        )

        assertThat(storeFiles.map { it.exists() }).containsExactly(false, false, false)
        assertThat(buildCount).isEqualTo(1)
        assertThat(File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).exists()).isTrue()
        result.destroy()
    }

    @Test
    fun `create - does not clear caches when the index already covers the event cache`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        File(cacheFolder, EVENT_CACHE_STORE_NAME).createNewFile()
        File(sessionFolder, SEARCH_INDEX_DIRECTORY).mkdirs()
        File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).createNewFile()
        var buildCount = 0
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
            clientBuilderProvider = FakeClientBuilderProvider {
                FakeFfiClientBuilder {
                    buildCount++
                    // The default clearCachesResult fails the test if it is ever invoked.
                    FakeFfiClient(withUtdHook = {})
                }
            },
        )

        val result = sut.create(
            aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret")
        )

        assertThat(buildCount).isEqualTo(1)
        assertThat(File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).exists()).isTrue()
        result.destroy()
    }

    @Test
    fun `create - does not clear caches when there is no event cache yet`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        var buildCount = 0
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
            clientBuilderProvider = FakeClientBuilderProvider {
                FakeFfiClientBuilder {
                    buildCount++
                    FakeFfiClient(withUtdHook = {})
                }
            },
        )

        val result = sut.create(
            aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret")
        )

        // Nothing was cached yet, so the index covers everything by construction.
        assertThat(buildCount).isEqualTo(1)
        assertThat(File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).exists()).isTrue()
        result.destroy()
    }

    @Test
    fun `create - deletes a stale index when message search is unavailable`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        File(cacheFolder, EVENT_CACHE_STORE_NAME).createNewFile()
        val indexDirectory = File(sessionFolder, SEARCH_INDEX_DIRECTORY).apply { mkdirs() }
        val coverageMarker = File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).apply { createNewFile() }
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to false),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )

        val result = sut.create(
            aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret")
        )

        // With search off, events reach the cache unindexed, so the index is silently stale.
        // It must be rebuilt from scratch on the next enable.
        assertThat(indexDirectory.exists()).isFalse()
        assertThat(coverageMarker.exists()).isFalse()
        result.destroy()
    }

    @Test
    fun `create - a healed session is not cleared again on the next restore`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        val storeFile = File(cacheFolder, EVENT_CACHE_STORE_NAME).apply { createNewFile() }
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )
        val sessionData = aSessionData(
            sessionPath = sessionFolder.absolutePath,
            cachePath = cacheFolder.absolutePath,
        ).copy(passphrase = "aSecret")

        sut.create(sessionData).destroy()
        assertThat(storeFile.exists()).isFalse()
        // In production the SDK recreates the store as soon as the client syncs.
        storeFile.createNewFile()

        val secondRestore = sut.create(sessionData)

        // The index directory and marker both survived the first restore, so the second one
        // trusts them instead of deleting the freshly re-fetched cache all over again.
        assertThat(storeFile.exists()).isTrue()
        secondRestore.destroy()
    }

    @Test
    fun `create with a client - records coverage for a fresh session`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        val sut = createRustMatrixClientFactory(
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )

        val result = sut.create(
            client = FakeFfiClient(withUtdHook = {}),
            sessionData = aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret"),
            isMessageSearchAvailable = true,
        )

        // A fresh login has no event cache, so the index covers everything by construction —
        // recording that spares the session a pointless cache clear on its first restore.
        assertThat(File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).exists()).isTrue()
        result.destroy()
    }

    @Test
    fun `create - does not write the coverage marker when deleting the event cache fails`() = runTest {
        val sessionFolder = temporaryFolder.newFolder("session")
        val cacheFolder = temporaryFolder.newFolder("cache")
        // A non-empty directory in place of the store file makes File.delete() fail.
        File(cacheFolder, EVENT_CACHE_STORE_NAME).apply {
            mkdirs()
            File(this, "child").createNewFile()
        }
        val sut = createRustMatrixClientFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
            workManagerScheduler = FakeWorkManagerScheduler(submitLambda = {}),
        )

        val result = sut.create(
            aSessionData(
                sessionPath = sessionFolder.absolutePath,
                cachePath = cacheFolder.absolutePath,
            ).copy(passphrase = "aSecret")
        )

        // The client is still usable, but the marker stays absent so the next start retries.
        assertThat(File(sessionFolder, SEARCH_INDEX_COVERAGE_MARKER).exists()).isFalse()
        result.destroy()
    }
}

fun TestScope.createRustMatrixClientFactory(
    cacheDirectory: File = File("/cache"),
    sessionStore: SessionStore = InMemorySessionStore(
        updateUserProfileResult = { _, _, _ -> },
    ),
    clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
    workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
    contentScannerUrlProvider: ContentScannerUrlProvider = { Result.success(null) },
    featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
) = RustMatrixClientFactory(
    cacheDirectory = cacheDirectory,
    appCoroutineScope = backgroundScope,
    coroutineDispatchers = testCoroutineDispatchers(),
    sessionStore = sessionStore,
    userAgentProvider = SimpleUserAgentProvider(),
    proxyProvider = FakeProxyProvider(),
    clock = FakeSystemClock(),
    analyticsService = FakeAnalyticsService(),
    featureFlagService = featureFlagService,
    timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
    clientBuilderProvider = clientBuilderProvider,
    sqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
    workManagerScheduler = workManagerScheduler,
    contentScannerUrlProvider = contentScannerUrlProvider,
)
