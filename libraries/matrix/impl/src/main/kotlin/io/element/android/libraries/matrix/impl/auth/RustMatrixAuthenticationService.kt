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
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.impl.RustMatrixClient
import io.element.android.libraries.matrix.impl.util.logError
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService @Inject constructor(
    private val baseDirectory: File,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val authService: AuthenticationService,
) : MatrixAuthenticationService {

    private var currentHomeserver = MutableStateFlow<MatrixHomeServerDetails?>(null)

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.isLoggedIn()
    }

    override suspend fun getLatestSessionId(): SessionId? = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()?.userId?.let { UserId(it) }
    }

    override suspend fun restoreSession(sessionId: SessionId) = withContext(coroutineDispatchers.io) {
        val sessionData = sessionStore.getSession(sessionId.value)
        if (sessionData != null) {
            try {
                val client = ClientBuilder()
                    .basePath(baseDirectory.absolutePath)
                    .homeserverUrl(sessionData.homeserverUrl)
                    .username(sessionData.userId)
                    .use { it.build() }
                client.restoreSession(sessionData.toSession())
                createMatrixClient(client)
            } catch (throwable: Throwable) {
                logError(throwable)
                null
            }
        } else null
    }

    override fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?> = currentHomeserver

    override suspend fun setHomeserver(homeserver: String) {
        withContext(coroutineDispatchers.io) {
            authService.configureHomeserver(homeserver)
            val homeServerDetails = authService.homeserverDetails()?.use { MatrixHomeServerDetails(it) }
            if (homeServerDetails != null) {
                currentHomeserver.value = homeServerDetails.copy(url = homeserver)
            }
        }
    }

    override suspend fun login(username: String, password: String): SessionId =
        withContext(coroutineDispatchers.io) {
            val client = try {
                authService.login(username, password, "ElementX Android", null)
            } catch (failure: Throwable) {
                Timber.e(failure, "Fail login")
                throw failure
            }
            val sessionData = client.use { it.session().toSessionData() }
            sessionStore.storeData(sessionData)
            SessionId(sessionData.userId)
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
    isSoftLogout = isSoftLogout,
    slidingSyncProxy = slidingSyncProxy,
)

private fun Session.toSessionData() = SessionData(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl,
    isSoftLogout = isSoftLogout,
    slidingSyncProxy = slidingSyncProxy,
)
