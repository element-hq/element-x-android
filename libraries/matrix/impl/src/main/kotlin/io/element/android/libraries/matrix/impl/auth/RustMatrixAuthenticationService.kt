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

package io.element.android.libraries.matrix.impl.auth

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.RustMatrixClient
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.OidcAuthenticationUrl
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.use
import java.io.File
import javax.inject.Inject
import org.matrix.rustcomponents.sdk.AuthenticationService as RustAuthenticationService

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService @Inject constructor(
    private val baseDirectory: File,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
) : MatrixAuthenticationService {

    private val authService: RustAuthenticationService = RustAuthenticationService(
        basePath = baseDirectory.absolutePath,
        passphrase = null,
        oidcClientMetadata = oidcClientMetadata,
        customSlidingSyncProxy = null
    )
    private var currentHomeserver = MutableStateFlow<MatrixHomeServerDetails?>(null)

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.isLoggedIn()
    }

    override suspend fun getLatestSessionId(): SessionId? = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()?.userId?.let { SessionId(it) }
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient> = withContext(coroutineDispatchers.io) {
        runCatching {
            val sessionData = sessionStore.getSession(sessionId.value)
            if (sessionData != null) {
                val client = ClientBuilder()
                    .basePath(baseDirectory.absolutePath)
                    .homeserverUrl(sessionData.homeserverUrl)
                    .username(sessionData.userId)
                    .use { it.build() }
                client.restoreSession(sessionData.toSession())
                createMatrixClient(client)
            } else {
                throw IllegalStateException("No session to restore with id $sessionId")
            }
        }.mapFailure { failure ->
            failure.mapAuthenticationException()
        }
    }

    override fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?> = currentHomeserver

    override suspend fun setHomeserver(homeserver: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                authService.configureHomeserver(homeserver)
                val homeServerDetails = authService.homeserverDetails()?.map()
                if (homeServerDetails != null) {
                    currentHomeserver.value = homeServerDetails.copy(url = homeserver)
                }
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }

    override suspend fun login(username: String, password: String): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                val client = authService.login(username, password, "ElementX Android", null)
                val sessionData = client.use { it.session().toSessionData() }
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }

    private var pendingUrlForOidcLogin: OidcAuthenticationUrl? = null

    override suspend fun getOidcUrl(): Result<OidcDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                val urlForOidcLogin = authService.urlForOidcLogin()
                val url = urlForOidcLogin.loginUrl()
                pendingUrlForOidcLogin = urlForOidcLogin
                OidcDetails(url)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                pendingUrlForOidcLogin?.close()
                pendingUrlForOidcLogin = null
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    /**
     * callbackUrl should be the uriRedirect from OidcClientMetadata (with all the parameters).
     */
    override suspend fun loginWithOidc(callbackUrl: String): Result<SessionId> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                val urlForOidcLogin = pendingUrlForOidcLogin ?: error("You need to call `getOidcUrl()` first")
                val client = authService.loginWithOidcCallback(urlForOidcLogin, callbackUrl)
                val sessionData = client.use { it.session().toSessionData() }
                pendingUrlForOidcLogin = null
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    private fun createMatrixClient(client: Client): MatrixClient {
        return RustMatrixClient(
            client = client,
            sessionStore = sessionStore,
            coroutineScope = coroutineScope,
            dispatchers = coroutineDispatchers,
            baseDirectory = baseDirectory,
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
)

private fun Session.toSessionData() = SessionData(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl,
    slidingSyncProxy = slidingSyncProxy,
)
