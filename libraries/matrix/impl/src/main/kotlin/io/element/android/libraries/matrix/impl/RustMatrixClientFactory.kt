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
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.use
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
) {
    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        val client = getBaseClientBuilder(sessionData.sessionPath, sessionData.passphrase)
            .serverNameOrHomeserverUrl(sessionData.homeserverUrl)
            .username(sessionData.userId)
            .use { it.build() }

        client.restoreSession(sessionData.toSession())

        val syncService = client.syncService()
            .withUtdHook(utdTracker)
            .finish()

        RustMatrixClient(
            client = client,
            syncService = syncService,
            sessionStore = sessionStore,
            appCoroutineScope = appCoroutineScope,
            dispatchers = coroutineDispatchers,
            baseDirectory = baseDirectory,
            baseCacheDirectory = cacheDirectory,
            clock = clock,
        )
    }

    internal fun getBaseClientBuilder(sessionPath: String, passphrase: String?): ClientBuilder {
        return ClientBuilder()
            .sessionPath(sessionPath)
            .passphrase(passphrase)
            .slidingSyncProxy(AuthenticationConfig.SLIDING_SYNC_PROXY_URL)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(userCertificatesProvider.provides())
            .autoEnableBackups(true)
            .autoEnableCrossSigning(true)
            // FIXME Quick and dirty fix for stopping version requests on startup https://github.com/matrix-org/matrix-rust-sdk/pull/1376
            .serverVersions(listOf("v1.0", "v1.1", "v1.2", "v1.3", "v1.4", "v1.5"))
            .run {
                // Workaround for non-nullable proxy parameter in the SDK, since each call to the ClientBuilder returns a new reference we need to keep
                proxyProvider.provides()?.let { proxy(it) } ?: this
            }
    }
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
