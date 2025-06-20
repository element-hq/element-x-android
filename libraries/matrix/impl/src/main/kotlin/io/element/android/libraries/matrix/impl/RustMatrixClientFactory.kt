/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.impl.analytics.UtdTracker
import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider
import io.element.android.libraries.matrix.impl.paths.SessionPaths
import io.element.android.libraries.matrix.impl.paths.getSessionPaths
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.matrix.impl.room.TimelineEventTypeFilterFactory
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.SlidingSyncVersion
import org.matrix.rustcomponents.sdk.SlidingSyncVersionBuilder
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk_crypto.CollectStrategy
import uniffi.matrix_sdk_crypto.TrustRequirement
import java.io.File
import javax.inject.Inject

class RustMatrixClientFactory @Inject constructor(
    private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val userAgentProvider: UserAgentProvider,
    private val userCertificatesProvider: UserCertificatesProvider,
    private val proxyProvider: ProxyProvider,
    private val clock: SystemClock,
    private val analyticsService: AnalyticsService,
    private val featureFlagService: FeatureFlagService,
    private val timelineEventTypeFilterFactory: TimelineEventTypeFilterFactory,
    private val clientBuilderProvider: ClientBuilderProvider,
) {
    private val sessionDelegate = RustClientSessionDelegate(sessionStore, appCoroutineScope, coroutineDispatchers)

    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        val client = getBaseClientBuilder(
            sessionPaths = sessionData.getSessionPaths(),
            passphrase = sessionData.passphrase,
            slidingSyncType = ClientBuilderSlidingSync.Restored,
        )
            .homeserverUrl(sessionData.homeserverUrl)
            .username(sessionData.userId)
            .use { it.build() }

        client.restoreSession(sessionData.toSession())

        create(client)
    }

    suspend fun create(client: Client): RustMatrixClient {
        val (anonymizedAccessToken, anonymizedRefreshToken) = client.session().anonymizedTokens()

        client.setUtdDelegate(UtdTracker(analyticsService))

        val syncService = client.syncService()
            .withOfflineMode()
            .finish()

        return RustMatrixClient(
            innerClient = client,
            baseDirectory = baseDirectory,
            sessionStore = sessionStore,
            appCoroutineScope = appCoroutineScope,
            sessionDelegate = sessionDelegate,
            innerSyncService = syncService,
            dispatchers = coroutineDispatchers,
            baseCacheDirectory = cacheDirectory,
            clock = clock,
            timelineEventTypeFilterFactory = timelineEventTypeFilterFactory,
            featureFlagService = featureFlagService,
        ).also {
            Timber.tag(it.toString()).d("Creating Client with access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'")
        }
    }

    internal suspend fun getBaseClientBuilder(
        sessionPaths: SessionPaths,
        passphrase: String?,
        slidingSyncType: ClientBuilderSlidingSync,
    ): ClientBuilder {
        return clientBuilderProvider.provide()
            .sessionPaths(
                dataPath = sessionPaths.fileDirectory.absolutePath,
                cachePath = sessionPaths.cacheDirectory.absolutePath,
            )
            .setSessionDelegate(sessionDelegate)
            .sessionPassphrase(passphrase)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(userCertificatesProvider.provides())
            .autoEnableBackups(true)
            .autoEnableCrossSigning(true)
            .roomKeyRecipientStrategy(
                strategy = if (featureFlagService.isFeatureEnabled(FeatureFlags.OnlySignedDeviceIsolationMode)) {
                    CollectStrategy.IDENTITY_BASED_STRATEGY
                } else {
                    CollectStrategy.ERROR_ON_VERIFIED_USER_PROBLEM
                }
            )
            .roomDecryptionTrustRequirement(
                trustRequirement = if (featureFlagService.isFeatureEnabled(FeatureFlags.OnlySignedDeviceIsolationMode)) {
                    TrustRequirement.CROSS_SIGNED_OR_LEGACY
                } else {
                    TrustRequirement.UNTRUSTED
                }
            )
            .enableShareHistoryOnInvite(featureFlagService.isFeatureEnabled(FeatureFlags.EnableKeyShareOnInvite))
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

sealed interface ClientBuilderSlidingSync {
    // The proxy will be supplied when restoring the Session.
    data object Restored : ClientBuilderSlidingSync

    // A Native Sliding Sync instance must be discovered whilst building the session.
    data object Discovered : ClientBuilderSlidingSync

    // Force using Native Sliding Sync.
    data object Native : ClientBuilderSlidingSync
}

private fun SessionData.toSession() = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    deviceId = deviceId,
    homeserverUrl = homeserverUrl,
    slidingSyncVersion = SlidingSyncVersion.NATIVE,
    oidcData = oidcData,
)
