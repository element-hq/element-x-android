/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.ClientBuilderEnterpriseHook
import io.element.android.libraries.androidutils.crypto.ClientSecret
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.data.ByteUnit
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.impl.analytics.UtdTracker
import io.element.android.libraries.matrix.impl.paths.getSessionPaths
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.matrix.impl.room.TimelineEventFilterFactory
import io.element.android.libraries.matrix.impl.scanner.RustContentScanner
import io.element.android.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.CrossProcessLockConfig
import org.matrix.rustcomponents.sdk.RequestConfig
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.SlidingSyncVersion
import org.matrix.rustcomponents.sdk.SlidingSyncVersionBuilder
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk_base.DmRoomDefinition
import uniffi.matrix_sdk_base.MediaRetentionPolicy
import uniffi.matrix_sdk_crypto.CollectStrategy
import uniffi.matrix_sdk_crypto.DecryptionSettings
import uniffi.matrix_sdk_crypto.TrustRequirement
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

@Inject
class RustMatrixClientFactory(
    @CacheDirectory private val cacheDirectory: File,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val userAgentProvider: UserAgentProvider,
    private val proxyProvider: ProxyProvider,
    private val clock: SystemClock,
    private val analyticsService: AnalyticsService,
    private val featureFlagService: FeatureFlagService,
    private val timelineEventFilterFactory: TimelineEventFilterFactory,
    private val clientBuilderProvider: ClientBuilderProvider,
    private val sqliteStoreBuilderProvider: SqliteStoreBuilderProvider,
    private val workManagerScheduler: WorkManagerScheduler,
    private val clientBuilderEnterpriseHook: ClientBuilderEnterpriseHook,
) {
    private val sessionDelegate = RustClientSessionDelegate(
        sessionStore = sessionStore,
        appCoroutineScope = appCoroutineScope,
        analyticsService = analyticsService,
    )

    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        // This secret is called 'passphrase' for historical reasons, but it can be a raw key or an actual passphrase
        val clientSecret = sessionData.passphrase?.let(ClientSecret::fromString)
        val sessionPaths = sessionData.getSessionPaths()
        val isMessageSearchAvailable = featureFlagService.isFeatureEnabled(FeatureFlags.MessageSearch)
        val indexDirectory = File(sessionPaths.fileDirectory, SEARCH_INDEX_DIRECTORY)
        val coverageMarker = File(sessionPaths.fileDirectory, SEARCH_INDEX_COVERAGE_MARKER)

        if (!isMessageSearchAvailable && indexDirectory.exists()) {
            // With search off, events reach the event cache unindexed and the index can never
            // catch up on them. Delete it so the next enable rebuilds it from a state where
            // coverage can actually be guaranteed, instead of resuming a silently stale index.
            // The marker goes first: if this is interrupted after a partial directory deletion,
            // a surviving marker would let a later restore trust the broken index.
            Timber.tag("RustMatrixClient").i("Message search is disabled, deleting the stale search index")
            if (!coverageMarker.exists() || coverageMarker.delete()) {
                indexDirectory.deleteRecursively()
            } else {
                Timber.tag("RustMatrixClient").w("Could not invalidate the search index coverage marker, keeping the index for now")
            }
        }

        // Events already in the event cache store when the index is created are re-hydrated from
        // disk on pagination, and the SDK drops those updates before the indexer — they would stay
        // unsearchable forever. Deleting the store once forces that history back through the
        // network, where indexing genuinely happens.
        val needsCoverageBootstrap = isMessageSearchAvailable &&
            !(indexDirectory.exists() && coverageMarker.exists()) &&
            File(sessionPaths.cacheDirectory, EVENT_CACHE_STORE_NAME).exists()

        var coverageEstablished = !needsCoverageBootstrap
        if (needsCoverageBootstrap) {
            Timber.tag("RustMatrixClient").i("The event cache predates the search index, deleting the event cache store so history gets re-fetched and indexed")
            coverageEstablished = deleteEventCacheStore(sessionPaths)
            if (!coverageEstablished) {
                Timber.tag("RustMatrixClient").w("Could not delete the event cache store, will retry on the next start")
            }
        }

        val client = getBaseClientBuilder(
            sessionPaths = sessionPaths,
            clientSecret = clientSecret,
            slidingSyncType = ClientBuilderSlidingSync.Restored,
            isMessageSearchAvailable = isMessageSearchAvailable,
        )
            .homeserverUrl(sessionData.homeserverUrl)
            .let { (clientBuilderEnterpriseHook(RustMatrixClientBuilder(it), SessionId(sessionData.userId)) as RustMatrixClientBuilder).inner }
            .use { it.build() }

        client.setMediaRetentionPolicy(
            MediaRetentionPolicy(
                // Make this 500MB instead of 400MB
                maxCacheSize = 500.megaBytes.into(ByteUnit.BYTES).toULong(),
                // This is the default value, but let's make it explicit
                maxFileSize = 20.megaBytes.into(ByteUnit.BYTES).toULong(),
                // Use 30 days instead of 60
                lastAccessExpiry = 30.days.toJavaDuration(),
                // This is the default value, but let's make it explicit
                cleanupFrequency = 1.days.toJavaDuration(),
            )
        )

        client.restoreSession(sessionData.toSession())

        if (isMessageSearchAvailable && coverageEstablished && !coverageMarker.exists()) {
            // Failing to record coverage must never fail the session restore; without the marker
            // the bootstrap simply runs again on the next start.
            runCatchingExceptions { coverageMarker.createNewFile() }
                .onFailure { Timber.tag("RustMatrixClient").w(it, "Failed to write the search index coverage marker") }
        }

        create(client, sessionData, isMessageSearchAvailable)
    }

    /**
     * Deletes the event cache store files. The WAL and SHM sidecars go first: if this is
     * interrupted, a database without its WAL is merely stale, while a fresh database next to a
     * leftover WAL is corruption waiting to be replayed.
     */
    private fun deleteEventCacheStore(sessionPaths: SessionPaths): Boolean =
        listOf("$EVENT_CACHE_STORE_NAME-wal", "$EVENT_CACHE_STORE_NAME-shm", EVENT_CACHE_STORE_NAME)
            .map { File(sessionPaths.cacheDirectory, it) }
            .all { !it.exists() || it.delete() }

    suspend fun create(
        client: Client,
        sessionData: SessionData,
        isMessageSearchAvailable: Boolean,
    ): RustMatrixClient {
        val (anonymizedAccessToken, anonymizedRefreshToken) = client.session().anonymizedTokens()

        // Must be called before creating the sync service, timelines etc.
        if (featureFlagService.isFeatureEnabled(FeatureFlags.AutomaticBackPagination)) {
            client.enableAutomaticBackpagination()
        }

        val sessionPaths = sessionData.getSessionPaths()
        val coverageMarker = File(sessionPaths.fileDirectory, SEARCH_INDEX_COVERAGE_MARKER)
        if (isMessageSearchAvailable && !coverageMarker.exists() && !File(sessionPaths.cacheDirectory, EVENT_CACHE_STORE_NAME).exists()) {
            // Nothing has been cached yet — a fresh login, or a heal that just cleared the
            // caches — so the index covers everything by construction. Recording that here
            // spares fresh sessions a pointless cache clear on their first restart.
            runCatchingExceptions { coverageMarker.createNewFile() }
                .onFailure { Timber.tag("RustMatrixClient").w(it, "Failed to write the search index coverage marker") }
        }

        client.setUtdDelegate(UtdTracker(analyticsService))

        val syncService = client.syncService()
            .withSharePos(true)
            .withOfflineMode()
            .withProfilesExtension()
            .finish()

        return RustMatrixClient(
            sessionPaths = sessionData.getSessionPaths(),
            innerClient = client,
            sessionStore = sessionStore,
            appCoroutineScope = appCoroutineScope,
            sessionDelegate = sessionDelegate,
            innerSyncService = syncService,
            dispatchers = coroutineDispatchers,
            baseCacheDirectory = cacheDirectory,
            clock = clock,
            timelineEventFilterFactory = timelineEventFilterFactory,
            featureFlagService = featureFlagService,
            analyticsService = analyticsService,
            workManagerScheduler = workManagerScheduler,
            contentScanner = client.contentScanner()?.let { RustContentScanner(client, it) },
            isMessageSearchAvailable = isMessageSearchAvailable,
        ).also {
            Timber.tag("RustMatrixClient").i("Creating Client with access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'")
        }
    }

    internal suspend fun getBaseClientBuilder(
        sessionPaths: SessionPaths,
        clientSecret: ClientSecret?,
        slidingSyncType: ClientBuilderSlidingSync,
        isMessageSearchAvailable: Boolean,
    ): ClientBuilder {
        return clientBuilderProvider.provide()
            .run {
                sqliteStoreBuilderProvider.provide(sessionPaths)
                    .secret(clientSecret)
                    .setupClientBuilder(this)
            }
            .setSessionDelegate(sessionDelegate)
            .userAgent(userAgentProvider.provide())
            .autoEnableBackups(true)
            .autoEnableCrossSigning(true)
            .roomKeyRecipientStrategy(
                strategy = if (featureFlagService.isFeatureEnabled(FeatureFlags.OnlySignedDeviceIsolationMode)) {
                    CollectStrategy.IDENTITY_BASED_STRATEGY
                } else {
                    CollectStrategy.ERROR_ON_VERIFIED_USER_PROBLEM
                }
            )
            .decryptionSettings(
                DecryptionSettings(
                    senderDeviceTrustRequirement = if (featureFlagService.isFeatureEnabled(FeatureFlags.OnlySignedDeviceIsolationMode)) {
                        TrustRequirement.CROSS_SIGNED_OR_LEGACY
                    } else {
                        TrustRequirement.UNTRUSTED
                    }
                )
            )
            .enableShareHistoryOnInvite(true)
            .threadsEnabled(featureFlagService.isFeatureEnabled(FeatureFlags.Threads), threadSubscriptions = false)
            .run {
                if (isMessageSearchAvailable) {
                    // The index is encrypted at rest with the same secret the SDK's SQLite stores
                    // use, or left unencrypted for sessions without one, matching those stores.
                    withSearchIndexStore(
                        path = File(sessionPaths.fileDirectory, SEARCH_INDEX_DIRECTORY).absolutePath,
                        password = clientSecret?.formattedAsString(),
                    )
                } else {
                    this
                }
            }
            .dmRoomDefinition(DmRoomDefinition.TWO_MEMBERS)
            .requestConfig(
                RequestConfig(
                    timeout = 30_000uL,
                    // retryLimit must be non-zero for the SDK to retry API calls in case of error (including 429 Too Many Requests error).
                    retryLimit = 3u,
                    // Use default values for the rest
                    maxConcurrentRequests = null,
                    maxRetryTime = null,
                )
            )
            // Make sure all built clients use the single process cross-process lock config
            .crossProcessLockConfig(CrossProcessLockConfig.SingleProcess)
            .run {
                // Apply sliding sync version settings
                when (slidingSyncType) {
                    ClientBuilderSlidingSync.Restored -> this
                    ClientBuilderSlidingSync.Discovered -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.DISCOVER_NATIVE)
                    ClientBuilderSlidingSync.Native -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.NATIVE)
                }
            }
            .run {
                // Workaround for non-nullable proxy parameter in the SDK, since each call to the ClientBuilder returns a new reference we need to keep
                proxyProvider.provides()?.let { proxy(it) } ?: this
            }
    }
}

/**
 * Directory holding the local message search index, under the session's file directory.
 * Not the cache directory: the index is not disposable, and a cache wipe should not destroy it.
 */
internal const val SEARCH_INDEX_DIRECTORY = "search-index"

/**
 * Marker file recording that the search index has seen every event since the event cache was last
 * empty. Events already in the cache store when the index is created are re-hydrated from disk on
 * pagination and never reach the indexer, so an index without this guarantee silently misses them.
 * Sibling of [SEARCH_INDEX_DIRECTORY] rather than inside it: the SDK owns that directory's layout.
 */
internal const val SEARCH_INDEX_COVERAGE_MARKER = "search-index.covered"

/**
 * The SDK's event cache store inside the session's cache directory. The name is fixed by the SDK's
 * SQLite store; its presence is how we detect that events were cached before the index existed.
 */
internal const val EVENT_CACHE_STORE_NAME = "matrix-sdk-event-cache.sqlite3"

sealed interface ClientBuilderSlidingSync {
    // The proxy will be supplied when restoring the Session.
    data object Restored : ClientBuilderSlidingSync

    // A Native Sliding Sync instance must be discovered whilst building the session.
    data object Discovered : ClientBuilderSlidingSync

    // Force using Native Sliding Sync.
    data object Native : ClientBuilderSlidingSync
}

fun SessionData.toSession() = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    deviceId = deviceId,
    homeserverUrl = homeserverUrl,
    slidingSyncVersion = SlidingSyncVersion.NATIVE,
    oauthData = oAuthData,
)
