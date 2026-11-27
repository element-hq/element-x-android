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
import io.element.android.libraries.matrix.impl.scanner.RustContentScanner
import io.element.android.libraries.matrix.impl.storage.SqliteStoreBuilderProvider
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.CrossProcessLockConfig
import org.matrix.rustcomponents.sdk.HttpSettings
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
    private val httpSettings: HttpSettings,
    private val requestConfig: RequestConfig,
    private val analyticsService: AnalyticsService,
    private val featureFlagService: FeatureFlagService,
    private val clientBuilderProvider: ClientBuilderProvider,
    private val sqliteStoreBuilderProvider: SqliteStoreBuilderProvider,
    private val clientBuilderEnterpriseHook: ClientBuilderEnterpriseHook,
    private val innerMatrixClientFactory: RustMatrixClient.Factory,
) {
    /**
     * Create a new [RustClientSessionDelegate]. Each client must have its own instance, since the delegate
     * keeps a reference to the client it handles the auth errors for.
     */
    private fun createSessionDelegate() = RustClientSessionDelegate(
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

        if (!isMessageSearchAvailable && indexDirectory.exists()) {
            // With search off, events reach the event cache unindexed and the index can never
            // catch up on them. Delete it so the next enable rebuilds it from a state where
            // coverage can actually be guaranteed, instead of resuming a silently stale index.
            Timber.tag("RustMatrixClient").i("Message search is disabled, deleting the stale search index")
            indexDirectory.deleteRecursively()
        }

        // Use a dedicated delegate for this client, so that an auth error only affects this session
        val sessionDelegate = createSessionDelegate()
        val client = getBaseClientBuilder(
            sessionPaths = sessionPaths,
            clientSecret = clientSecret,
            slidingSyncType = ClientBuilderSlidingSync.Restored,
            isMessageSearchAvailable = isMessageSearchAvailable,
            sessionDelegate = sessionDelegate,
            autoEnableEncryption = true,
        )
            .homeserverUrl(sessionData.homeserverUrl)
            .enableAutomaticBackPagination(featureFlagService.isFeatureEnabled(FeatureFlags.AutomaticBackPagination))
            .let {
                (clientBuilderEnterpriseHook.beforeClientCreationWithSession(
                    clientBuilder = RustMatrixClientBuilder(it),
                    sessionId = SessionId(sessionData.userId),
                ) as RustMatrixClientBuilder).inner
            }
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

        val (anonymizedAccessToken, anonymizedRefreshToken) = client.session().anonymizedTokens()

        client.setUtdDelegate(UtdTracker(analyticsService))

        // Each builder method returns a new builder instance, so all of them need to be closed
        val syncService = client.syncService()
            .use { it.withSharePos(true) }
            .use { it.withOfflineMode() }
            .use { it.finish() }

        innerMatrixClientFactory.create(
            sessionPaths = sessionData.getSessionPaths(),
            innerClient = client,
            innerSyncService = syncService,
            baseCacheDirectory = cacheDirectory,
            contentScanner = client.contentScanner()?.let { RustContentScanner(client, it, coroutineDispatchers.io) },
            isMessageSearchAvailable = isMessageSearchAvailable,
            sessionDelegate = sessionDelegate,
        ).also {
            Timber.tag("RustMatrixClient").i("Creating Client with access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'")
        }
    }

    internal suspend fun getBaseClientBuilder(
        sessionPaths: SessionPaths,
        clientSecret: ClientSecret?,
        slidingSyncType: ClientBuilderSlidingSync,
        isMessageSearchAvailable: Boolean,
        sessionDelegate: ClientSessionDelegate,
        autoEnableEncryption: Boolean,
    ): ClientBuilder {
        return clientBuilderProvider.provide()
            .run {
                sqliteStoreBuilderProvider.provide(sessionPaths)
                    .secret(clientSecret)
                    .setupClientBuilder(this)
            }
            .setSessionDelegate(sessionDelegate)
            .httpSettings(httpSettings)
            // Only the final client must set up the backup and the cross-signing. A client used to login shares the same store,
            // and its SDK initialization task could still be running when the final client is created.
            .autoEnableBackups(autoEnableEncryption)
            .autoEnableCrossSigning(autoEnableEncryption)
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
                        key = clientSecret?.formattedAsString(),
                    )
                } else {
                    this
                }
            }
            .dmRoomDefinition(DmRoomDefinition.TWO_MEMBERS)
            .requestConfig(requestConfig)
            // Make sure all built clients use the single process cross-process lock config
            .crossProcessLockConfig(CrossProcessLockConfig.SingleProcess)
            .run {
                // Apply sliding sync version settings
                when (slidingSyncType) {
                    ClientBuilderSlidingSync.Restored -> this
                    ClientBuilderSlidingSync.Discovered -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.DISCOVER_UNSTABLE)
                    ClientBuilderSlidingSync.Native -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.UNSTABLE)
                }
            }
            .let {
                (clientBuilderEnterpriseHook.beforeClientCreation(RustMatrixClientBuilder(it)) as RustMatrixClientBuilder).inner
            }
    }
}

/**
 * Directory holding the local message search index, under the session's file directory.
 * Not the cache directory: the index is not disposable, and a cache wipe should not destroy it.
 */
internal const val SEARCH_INDEX_DIRECTORY = "search-index"

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
    slidingSyncVersion = SlidingSyncVersion.UNSTABLE,
    oauthData = oAuthData,
)
