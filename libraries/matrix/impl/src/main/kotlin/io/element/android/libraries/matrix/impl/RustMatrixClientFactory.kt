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
            slidingSync = if (appPreferencesStore.isSimplifiedSlidingSyncEnabledFlow().first()) {
                ClientBuilderSlidingSync.Simplified
            } else {
                ClientBuilderSlidingSync.Restored
            },
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

            // Make sure the session delegate has a reference to the client to be able to logout on auth error
            sessionDelegate.client = it
        }
    }

    internal fun getBaseClientBuilder(
        sessionPaths: SessionPaths,
        passphrase: String?,
        slidingSyncProxy: String? = null,
        slidingSync: ClientBuilderSlidingSync,
    ): ClientBuilder {
        return ClientBuilder()
            .sessionPaths(
                dataPath = sessionPaths.fileDirectory.absolutePath,
                cachePath = sessionPaths.cacheDirectory.absolutePath,
            )
            .passphrase(passphrase)
            .slidingSyncProxy(slidingSyncProxy)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(userCertificatesProvider.provides())
            .autoEnableBackups(true)
            .autoEnableCrossSigning(true)
            .run {
                when (slidingSync) {
                    ClientBuilderSlidingSync.Restored -> this
                    ClientBuilderSlidingSync.Discovered -> requiresSlidingSync()
                    ClientBuilderSlidingSync.Simplified -> simplifiedSlidingSync(true)
                }
            }
            .run {
                // Workaround for non-nullable proxy parameter in the SDK, since each call to the ClientBuilder returns a new reference we need to keep
                proxyProvider.provides()?.let { proxy(it) } ?: this
            }
    }
}

enum class ClientBuilderSlidingSync {
    // The proxy will be supplied when restoring the Session.
    Restored,

    // A proxy must be discovered whilst building the session.
    Discovered,

    // Use Simplified Sliding Sync (discovery isn't a thing yet).
    Simplified,
}

private fun SessionData.toSession() = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    deviceId = deviceId,
    homeserverUrl = homeserverUrl,
    slidingSyncProxy = slidingSyncProxy,
    oidcData = oidcData,
)
