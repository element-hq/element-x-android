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
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.RustMatrixClientFactory
import io.element.android.libraries.matrix.impl.auth.qrlogin.QrErrorMapper
import io.element.android.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import io.element.android.libraries.matrix.impl.auth.qrlogin.toStep
import io.element.android.libraries.matrix.impl.exception.mapClientException
import io.element.android.libraries.matrix.impl.keys.PassphraseGenerator
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import org.matrix.rustcomponents.sdk.QrCodeDecodeException
import org.matrix.rustcomponents.sdk.QrLoginProgress
import org.matrix.rustcomponents.sdk.QrLoginProgressListener
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk.OidcAuthorizationData
import java.io.File
import java.util.UUID
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService @Inject constructor(
    baseDirectory: File,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
    private val passphraseGenerator: PassphraseGenerator,
    private val oidcConfigurationProvider: OidcConfigurationProvider,
) : MatrixAuthenticationService {
    // Passphrase which will be used for new sessions. Existing sessions will use the passphrase
    // stored in the SessionData.
    private val pendingPassphrase = getDatabasePassphrase()
    private val sessionPath = File(baseDirectory, UUID.randomUUID().toString()).absolutePath
    private var currentClient: Client? = null
    private var currentHomeserver = MutableStateFlow<MatrixHomeServerDetails?>(null)

    override fun loggedInStateFlow(): Flow<LoggedInState> {
        return sessionStore.isLoggedIn()
    }

    override suspend fun getLatestSessionId(): SessionId? = withContext(coroutineDispatchers.io) {
        sessionStore.getLatestSession()?.userId?.let { SessionId(it) }
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient> = withContext(coroutineDispatchers.io) {
        runCatching {
            val sessionData = sessionStore.getSession(sessionId.value)
            if (sessionData != null) {
                if (sessionData.isTokenValid) {
                    // Use the sessionData.passphrase, which can be null for a previously created session
                    if (sessionData.passphrase == null) {
                        Timber.w("Restoring a session without a passphrase")
                    } else {
                        Timber.w("Restoring a session with a passphrase")
                    }
                    rustMatrixClientFactory.create(sessionData)
                } else {
                    error("Token is not valid")
                }
            } else {
                error("No session to restore with id $sessionId")
            }
        }.mapFailure { failure ->
            failure.mapClientException()
        }
    }

    private fun getDatabasePassphrase(): String? {
        val passphrase = passphraseGenerator.generatePassphrase()
        if (passphrase != null) {
            Timber.w("New sessions will be encrypted with a passphrase")
        }
        return passphrase
    }

    override fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?> = currentHomeserver

    override suspend fun setHomeserver(homeserver: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                val client = getBaseClientBuilder()
                    .serverNameOrHomeserverUrl(homeserver)
                    .build()
                currentClient = client
                val homeServerDetails = client.homeserverLoginDetails().map()
                currentHomeserver.value = homeServerDetails.copy(url = homeserver)
            }.onFailure {
                clear()
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }

    override suspend fun login(username: String, password: String): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                client.login(username, password, "Element X Android", null)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.PASSWORD,
                        passphrase = pendingPassphrase,
                        sessionPath = sessionPath,
                    )
                clear()
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }

    private var pendingOidcAuthorizationData: OidcAuthorizationData? = null

    override suspend fun getOidcUrl(): Result<OidcDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val oidcAuthenticationData = client.urlForOidcLogin(oidcConfigurationProvider.get())
                val url = oidcAuthenticationData.loginUrl()
                pendingOidcAuthorizationData = oidcAuthenticationData
                OidcDetails(url)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                pendingOidcAuthorizationData?.close()
                pendingOidcAuthorizationData = null
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
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val urlForOidcLogin = pendingOidcAuthorizationData ?: error("You need to call `getOidcUrl()` first")
                client.loginWithOidcCallback(urlForOidcLogin, callbackUrl)
                val sessionData = client.session().toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.OIDC,
                    passphrase = pendingPassphrase,
                    sessionPath = sessionPath,
                )
                clear()
                pendingOidcAuthorizationData?.close()
                pendingOidcAuthorizationData = null
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) =
        withContext(coroutineDispatchers.io) {
            runCatching {
                val client = rustMatrixClientFactory.getBaseClientBuilder(
                    sessionPath = sessionPath,
                    passphrase = pendingPassphrase,
                    slidingSyncProxy = AuthenticationConfig.SLIDING_SYNC_PROXY_URL,
                )
                    .buildWithQrCode(
                        qrCodeData = (qrCodeData as SdkQrCodeLoginData).rustQrCodeData,
                        oidcConfiguration = oidcConfigurationProvider.get(),
                        progressListener = object : QrLoginProgressListener {
                            override fun onUpdate(state: QrLoginProgress) {
                                Timber.d("QR Code login progress: $state")
                                progress(state.toStep())
                            }
                        }
                    )
                client.use { rustClient ->
                    val sessionData = rustClient.session()
                        .toSessionData(
                            isTokenValid = true,
                            loginType = LoginType.QR,
                            passphrase = pendingPassphrase,
                            sessionPath = sessionPath,
                        )
                    sessionStore.storeData(sessionData)
                    SessionId(sessionData.userId)
                }
            }.mapFailure {
                when (it) {
                    is QrCodeDecodeException -> QrErrorMapper.map(it)
                    is HumanQrLoginException -> QrErrorMapper.map(it)
                    else -> it
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
                Timber.e(throwable, "Failed to login with QR code")
            }
    }

    private fun getBaseClientBuilder() = rustMatrixClientFactory
        .getBaseClientBuilder(
            sessionPath = sessionPath,
            passphrase = pendingPassphrase,
            slidingSyncProxy = AuthenticationConfig.SLIDING_SYNC_PROXY_URL,
        )
        .requiresSlidingSync()

    private fun clear() {
        currentClient?.close()
        currentClient = null
    }
}
