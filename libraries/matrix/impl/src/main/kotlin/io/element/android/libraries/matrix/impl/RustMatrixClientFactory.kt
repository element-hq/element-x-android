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
import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider
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
    private val clock: SystemClock,
) {
    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        val client = ClientBuilder()
            .basePath(baseDirectory.absolutePath)
            .homeserverUrl(sessionData.homeserverUrl)
            .username(sessionData.userId)
            .passphrase(sessionData.passphrase)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(userCertificatesProvider.provides())
            // FIXME Quick and dirty fix for stopping version requests on startup https://github.com/matrix-org/matrix-rust-sdk/pull/1376
            .serverVersions(listOf("v1.0", "v1.1", "v1.2", "v1.3", "v1.4", "v1.5"))
            .use { it.build() }

        client.restoreSession(sessionData.toSession())

        val syncService = client.syncService()
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
