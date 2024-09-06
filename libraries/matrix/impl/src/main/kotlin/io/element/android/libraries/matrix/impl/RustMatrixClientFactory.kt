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

package io.element.android.libraries.matrix.impl

import io.element.android.appconfig.AuthenticationConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.matrix.impl.analytics.UtdTracker
import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider
import io.element.android.libraries.matrix.impl.paths.SessionPaths
import io.element.android.libraries.matrix.impl.paths.getSessionPaths
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.SlidingSyncVersion
import org.matrix.rustcomponents.sdk.SlidingSyncVersionBuilder
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class RustMatrixClientFactory @Inject constructor(
    private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
    private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val userAgentProvider: UserAgentProvider,
    private val userCertificatesProvider: UserCertificatesProvider,
    private val proxyProvider: ProxyProvider,
    private val clock: SystemClock,
    private val utdTracker: UtdTracker,
    private val appPreferencesStore: AppPreferencesStore,
) {
    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        val sessionDelegate = RustClientSessionDelegate(sessionStore, appCoroutineScope, coroutineDispatchers)
        val client = getBaseClientBuilder(
            sessionPaths = sessionData.getSessionPaths(),
            passphrase = sessionData.passphrase,
            restore = true,
        )
            .homeserverUrl(sessionData.homeserverUrl)
            .username(sessionData.userId)
            .setSessionDelegate(sessionDelegate)
            .use { it.build() }

        client.restoreSession(sessionData.toSession())

        val syncService = client.syncService()
            .withUtdHook(utdTracker)
            .finish()

        val (anonymizedAccessToken, anonymizedRefreshToken) = sessionData.anonymizedTokens()

        RustMatrixClient(
            client = client,
            syncService = syncService,
            sessionStore = sessionStore,
            appCoroutineScope = appCoroutineScope,
            dispatchers = coroutineDispatchers,
            baseDirectory = baseDirectory,
            baseCacheDirectory = cacheDirectory,
            clock = clock,
            sessionDelegate = sessionDelegate,
        ).also {
            Timber.tag(it.toString()).d("Creating Client with access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'")
        }
    }

    internal suspend fun getBaseClientBuilder(
        sessionPaths: SessionPaths,
        passphrase: String?,
        restore: Boolean,
    ): ClientBuilder {
        val slidingSync = when {
            // Always check restore first, since otherwise other values could accidentally override the already persisted config
            restore -> ClientBuilderSlidingSync.Restored
            AuthenticationConfig.SLIDING_SYNC_PROXY_URL != null -> ClientBuilderSlidingSync.CustomProxy(AuthenticationConfig.SLIDING_SYNC_PROXY_URL!!)
            appPreferencesStore.isSimplifiedSlidingSyncEnabledFlow().first() -> ClientBuilderSlidingSync.Simplified
            else -> ClientBuilderSlidingSync.Discovered
        }

        return ClientBuilder()
            .sessionPaths(
                dataPath = sessionPaths.fileDirectory.absolutePath,
                cachePath = sessionPaths.cacheDirectory.absolutePath,
            )
            .passphrase(passphrase)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(userCertificatesProvider.provides())
            .autoEnableBackups(true)
            .autoEnableCrossSigning(true)
            .run {
                // Apply sliding sync version settings
                when (slidingSync) {
                    ClientBuilderSlidingSync.Restored -> this
                    is ClientBuilderSlidingSync.CustomProxy -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.Proxy(slidingSync.url))
                    ClientBuilderSlidingSync.Discovered -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.DiscoverProxy)
                    ClientBuilderSlidingSync.Simplified -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.DiscoverNative)
                    ClientBuilderSlidingSync.ForcedSimplified -> slidingSyncVersionBuilder(SlidingSyncVersionBuilder.Native)
                }
            }
            .run {
                // Workaround for non-nullable proxy parameter in the SDK, since each call to the ClientBuilder returns a new reference we need to keep
                proxyProvider.provides()?.let { proxy(it) } ?: this
            }
    }
}

sealed interface ClientBuilderSlidingSync {
    // The proxy is set by the user.
    data class CustomProxy(val url: String) : ClientBuilderSlidingSync

    // The proxy will be supplied when restoring the Session.
    data object Restored : ClientBuilderSlidingSync

    // A proxy must be discovered whilst building the session.
    data object Discovered : ClientBuilderSlidingSync

    // Use Simplified Sliding Sync.
    data object Simplified : ClientBuilderSlidingSync

    // Force using Simplified Sliding Sync.
    // TODO allow the user to select between proxy, simplified or force simplified in developer options.
    data object ForcedSimplified : ClientBuilderSlidingSync
}

private fun SessionData.toSession() = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    deviceId = deviceId,
    homeserverUrl = homeserverUrl,
    slidingSyncVersion = slidingSyncProxy?.let(SlidingSyncVersion::Proxy) ?: SlidingSyncVersion.Native,
    oidcData = oidcData,
)
