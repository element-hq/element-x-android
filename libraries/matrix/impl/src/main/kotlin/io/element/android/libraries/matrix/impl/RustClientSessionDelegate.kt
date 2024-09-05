/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.matrix.impl.paths.getSessionPaths
import io.element.android.libraries.matrix.impl.util.anonymizedTokens
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.ClientDelegate
import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.Session
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is responsible for handling the session data for the Rust SDK.
 *
 * It implements both [ClientSessionDelegate] and [ClientDelegate] to react to session data updates and auth errors.
 *
 * IMPORTANT: you must set the [client] property as soon as possible so [didReceiveAuthError] can work properly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RustClientSessionDelegate(
    private val sessionStore: SessionStore,
    private val appCoroutineScope: CoroutineScope,
    coroutineDispatchers: CoroutineDispatchers,
) : ClientSessionDelegate, ClientDelegate {
    private val clientLog = Timber.tag("$this")

    // Used to ensure several calls to `didReceiveAuthError` don't trigger multiple logouts
    private val isLoggingOut = AtomicBoolean(false)

    // To make sure only one coroutine affecting the token persistence can run at a time
    private val updateTokensDispatcher = coroutineDispatchers.io.limitedParallelism(1)

    // This Client needs to be set up as soon as possible so `didReceiveAuthError` can work properly.
    private var client: RustMatrixClient? = null

    /**
     * Sets the [ClientDelegate] for the [RustMatrixClient], and keeps a reference to the client so it can be used later.
     */
    fun bindClient(client: RustMatrixClient) {
        this.client = client
        client.setDelegate(this)
    }

    override fun saveSessionInKeychain(session: Session) {
        appCoroutineScope.launch(updateTokensDispatcher) {
            val existingData = sessionStore.getSession(session.userId) ?: return@launch
            val (anonymizedAccessToken, anonymizedRefreshToken) = session.anonymizedTokens()
            clientLog.d(
                "Saving new session data with token: access token '$anonymizedAccessToken' and refresh token '$anonymizedRefreshToken'. " +
                    "Was token valid: ${existingData.isTokenValid}"
            )
            val newData = session.toSessionData(
                isTokenValid = true,
                loginType = existingData.loginType,
                passphrase = existingData.passphrase,
                sessionPaths = existingData.getSessionPaths(),
            )
            sessionStore.updateData(newData)
            clientLog.d("Saved new session data with access token: '$anonymizedAccessToken'.")
        }.invokeOnCompletion {
            if (it != null) {
                clientLog.e(it, "Failed to save new session data.")
            }
        }
    }

    override fun didReceiveAuthError(isSoftLogout: Boolean) {
        clientLog.w("didReceiveAuthError(isSoftLogout=$isSoftLogout)")
        if (isLoggingOut.getAndSet(true).not()) {
            clientLog.v("didReceiveAuthError -> do the cleanup")
            // TODO handle isSoftLogout parameter.
            appCoroutineScope.launch(updateTokensDispatcher) {
                val currentClient = client
                if (currentClient == null) {
                    clientLog.w("didReceiveAuthError -> no client, exiting")
                    isLoggingOut.set(false)
                    return@launch
                }
                val existingData = sessionStore.getSession(currentClient.sessionId.value)
                val (anonymizedAccessToken, anonymizedRefreshToken) = existingData.anonymizedTokens()
                clientLog.d(
                    "Removing session data with access token '$anonymizedAccessToken' " +
                        "and refresh token '$anonymizedRefreshToken'."
                )
                if (existingData != null) {
                    // Set isTokenValid to false
                    val newData = existingData.copy(isTokenValid = false)
                    sessionStore.updateData(newData)
                    clientLog.d("Invalidated session data with access token: '$anonymizedAccessToken'.")
                } else {
                    clientLog.d("No session data found.")
                }
                client?.logout(userInitiated = false, ignoreSdkError = true)
            }.invokeOnCompletion {
                if (it != null) {
                    clientLog.e(it, "Failed to remove session data.")
                }
            }
        } else {
            clientLog.v("didReceiveAuthError -> already cleaning up")
        }
    }

    override fun didRefreshTokens() {
        // This is done in `saveSessionInKeychain(Session)` instead.
    }

    override fun retrieveSessionFromKeychain(userId: String): Session {
        // This should never be called, as it's only used for multi-process setups
        error("retrieveSessionFromKeychain should never be called for Android")
    }
}
