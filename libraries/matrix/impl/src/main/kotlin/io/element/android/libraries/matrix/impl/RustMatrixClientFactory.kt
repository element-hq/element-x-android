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
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import java.security.KeyStore
import javax.inject.Inject

class RustMatrixClientFactory @Inject constructor(
    private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
    private val appCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val userAgentProvider: UserAgentProvider,
    private val clock: SystemClock,
) {
    suspend fun create(sessionData: SessionData): RustMatrixClient = withContext(coroutineDispatchers.io) {
        val client = ClientBuilder()
            .basePath(baseDirectory.absolutePath)
            .homeserverUrl(sessionData.homeserverUrl)
            .username(sessionData.userId)
            .passphrase(sessionData.passphrase)
            .userAgent(userAgentProvider.provide())
            .addRootCertificates(getAdditionalCertificates())
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

/**
* Get additional user-installed certificates from the `AndroidCAStore` `Keystore`.
*
* The Rust HTTP client doesn't include user-installed certificates in its internal certificate
* store. This means that whatever the user installs will be ignored.
*
* While most users don't need user-installed certificates some special deployments or debugging
* setups using a proxy might want to use them.
*
* @return A list of byte arrays where each byte array is a single user-installed certificate
*         in encoded form.
*/
fun getAdditionalCertificates(): List<ByteArray> {
    val certs = mutableListOf<ByteArray>()

    // At least for API 34 the `AndroidCAStore` `Keystore` type contained user certificates as well.
    // I have not found this to be documented anywhere.
    val keyStore: KeyStore = KeyStore.getInstance("AndroidCAStore").apply {
        load(null)
    }

    val aliases = keyStore.aliases()

    while (aliases.hasMoreElements()) {
        val alias = aliases.nextElement()
        val entry = keyStore.getEntry(alias, null)

        if (entry is KeyStore.TrustedCertificateEntry) {
            // The certificate alias always contains the prefix `system` or
            // `user` and the MD5 subject hash separated by a colon.
            //
            // The subject hash can be calculated using openssl as such:
            //     openssl x509 -subject_hash_old -noout -in mycert.cer
            //
            // Again, I have not found this to be documented somewhere.
            if (alias.startsWith("user")) {
                certs.add(entry.trustedCertificate.encoded)
            }
        }
    }

    // Let's at least log the number of user-installed certificates we found,
    // since the alias isn't particularly useful nor does the issuer seem to
    // be easily available.
    val certCount = certs.count()

    Timber.i("Found $certCount additional user-provided certificates.")

    return certs
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
