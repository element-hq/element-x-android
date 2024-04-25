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
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
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
import io.element.android.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import io.element.android.libraries.matrix.impl.auth.qrlogin.toStep
import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider
import io.element.android.libraries.matrix.impl.exception.mapClientException
import io.element.android.libraries.matrix.impl.keys.PassphraseGenerator
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.matrix.impl.proxy.ProxyProvider
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.network.useragent.UserAgentProvider
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.OidcAuthenticationData
import org.matrix.rustcomponents.sdk.QrLoginProgress
import org.matrix.rustcomponents.sdk.QrLoginProgressListener
import org.matrix.rustcomponents.sdk.RecoveryState
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.VerificationState
import org.matrix.rustcomponents.sdk.VerificationStateListener
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import org.matrix.rustcomponents.sdk.AuthenticationService as RustAuthenticationService

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService @Inject constructor(
    baseDirectory: File,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    userAgentProvider: UserAgentProvider,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
    private val passphraseGenerator: PassphraseGenerator,
    userCertificatesProvider: UserCertificatesProvider,
    proxyProvider: ProxyProvider,
    private val buildMeta: BuildMeta,
) : MatrixAuthenticationService {
    // Passphrase which will be used for new sessions. Existing sessions will use the passphrase
    // stored in the SessionData.
    private val pendingPassphrase = getDatabasePassphrase()
    private val authService: RustAuthenticationService = RustAuthenticationService(
        basePath = baseDirectory.absolutePath,
        passphrase = pendingPassphrase,
        proxy = proxyProvider.provides(),
        userAgent = userAgentProvider.provide(),
        additionalRootCertificates = userCertificatesProvider.provides(),
        oidcConfiguration = oidcConfiguration,
        customSlidingSyncProxy = null,
        sessionDelegate = null,
        crossProcessRefreshLockId = null,
    )
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
        // TODO Remove this if block at some point
        // Return a passphrase only for debug and nightly build for now
        if (buildMeta.buildType == BuildType.RELEASE) {
            Timber.w("New sessions will not be encrypted with a passphrase (release build)")
            return null
        }

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
                val client = authService.login(username, password, "Element X Android", null)
                val sessionData = client.use {
                    it.session().toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.PASSWORD,
                        passphrase = pendingPassphrase,
                        needsVerification = true,
                    )
                }
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }

    private var pendingOidcAuthenticationData: OidcAuthenticationData? = null

    override suspend fun getOidcUrl(): Result<OidcDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                val oidcAuthenticationData = authService.urlForOidcLogin()
                val url = oidcAuthenticationData.loginUrl()
                pendingOidcAuthenticationData = oidcAuthenticationData
                OidcDetails(url)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatching {
                pendingOidcAuthenticationData?.close()
                pendingOidcAuthenticationData = null
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
                val urlForOidcLogin = pendingOidcAuthenticationData ?: error("You need to call `getOidcUrl()` first")
                val client = authService.loginWithOidcCallback(urlForOidcLogin, callbackUrl)
                val sessionData = client.use {
                    it.session().toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.OIDC,
                        passphrase = pendingPassphrase,
                        needsVerification = true,
                    )
                }
                pendingOidcAuthenticationData?.close()
                pendingOidcAuthenticationData = null
                sessionStore.storeData(sessionData)
                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) = runCatching {
        val client = rustMatrixClientFactory.getBaseClientBuilder()
            .passphrase(pendingPassphrase)
            .buildWithQrCode(
                qrCodeData = (qrCodeData as SdkQrCodeLoginData).rustQrCodeData,
                oidcConfiguration = oidcConfiguration,
                progressListener = object : QrLoginProgressListener {
                    override fun onUpdate(state: QrLoginProgress) {
                        println("QR Code login progress: $state")
                        progress(state.toStep())
                    }
                }
            )
        val sessionData = client.session()
            .toSessionData(
                isTokenValid = true,
                loginType = LoginType.OIDC,
                passphrase = pendingPassphrase,
                needsVerification = true,
            )
        pendingOidcAuthenticationData?.close()
        pendingOidcAuthenticationData = null
        sessionStore.storeData(sessionData)

        val recoveryMutex = Mutex(locked = true)
        val verificationMutex = Mutex(locked = true)
        val encryptionService = client.encryption()

        val syncService = client.syncService().finish()
        syncService.start()

        var recoveryStateHandle: TaskHandle? = null
        var verificationStateHandle: TaskHandle? = null
        recoveryStateHandle = encryptionService.recoveryStateListener(object : RecoveryStateListener {
            override fun onUpdate(status: RecoveryState) {
                Timber.d("QR Login recovery state: $status")
                if (status == RecoveryState.ENABLED) {
                    recoveryMutex.unlock()
                    recoveryStateHandle?.cancelAndDestroy()
                }
            }
        })
        verificationStateHandle = encryptionService.verificationStateListener(object : VerificationStateListener {
            override fun onUpdate(status: VerificationState) {
                Timber.d("QR Login Verification state: $status")
                if (status == VerificationState.VERIFIED) {
                    verificationMutex.unlock()
                    verificationStateHandle?.cancelAndDestroy()
                }
            }
        })
        verificationMutex.lock()
        recoveryMutex.lock()

        syncService.stop()
        syncService.destroy()
        client.destroy()

        SessionId(sessionData.userId)
    }.onFailure {
        it.printStackTrace()
    }
}
