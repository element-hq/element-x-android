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

package io.element.android.x.matrix.auth

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.x.di.AppScope
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.RustMatrixClient
import io.element.android.x.matrix.core.SessionId
import io.element.android.x.matrix.session.SessionStore
import io.element.android.x.matrix.session.sessionId
import io.element.android.x.matrix.util.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.AuthenticationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustMatrixAuthenticationService @Inject constructor(
    private val baseDirectory: File,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val authService: AuthenticationService,
) : MatrixAuthenticationService {

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.isLoggedIn()
    }

    override suspend fun getLatestSessionId(): SessionId? = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()?.sessionId()
    }

    override suspend fun restoreSession() = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()
            ?.let { session ->
                try {
                    ClientBuilder()
                        .basePath(baseDirectory.absolutePath)
                        .username(session.userId)
                        .build().apply {
                            restoreSession(session)
                        }
                } catch (throwable: Throwable) {
                    logError(throwable)
                    null
                }
            }?.let {
                createMatrixClient(it)
            }
    }

    override fun getHomeserver(): String? = authService.homeserverDetails()?.url()

    override fun getHomeserverOrDefault(): String = getHomeserver() ?: "matrix.org"

    override suspend fun setHomeserver(homeserver: String) {
        withContext(coroutineDispatchers.io) {
            authService.configureHomeserver(homeserver)
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
            val session = client.session()
            sessionStore.storeData(session)
            session.sessionId()
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
