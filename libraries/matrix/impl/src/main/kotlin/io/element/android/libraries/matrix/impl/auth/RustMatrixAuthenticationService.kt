/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.api.auth.OidcPrompt
import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.ClientBuilderSlidingSync
import io.element.android.libraries.matrix.impl.RustMatrixClientFactory
import io.element.android.libraries.matrix.impl.auth.qrlogin.QrErrorMapper
import io.element.android.libraries.matrix.impl.auth.qrlogin.SdkQrCodeLoginData
import io.element.android.libraries.matrix.impl.auth.qrlogin.toStep
import io.element.android.libraries.matrix.impl.exception.mapClientException
import io.element.android.libraries.matrix.impl.keys.PassphraseGenerator
import io.element.android.libraries.matrix.impl.mapper.toSessionData
import io.element.android.libraries.matrix.impl.paths.SessionPaths
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.libraries.sessionstorage.api.LoginType
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.HumanQrLoginException
import org.matrix.rustcomponents.sdk.QrCodeData
import org.matrix.rustcomponents.sdk.QrCodeDecodeException
import org.matrix.rustcomponents.sdk.QrLoginProgress
import org.matrix.rustcomponents.sdk.QrLoginProgressListener
import timber.log.Timber
import uniffi.matrix_sdk.OAuthAuthorizationData

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RustMatrixAuthenticationService(
    private val sessionPathsFactory: SessionPathsFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val sessionStore: SessionStore,
    private val rustMatrixClientFactory: RustMatrixClientFactory,
    private val passphraseGenerator: PassphraseGenerator,
    private val oidcConfigurationProvider: OidcConfigurationProvider,
) : MatrixAuthenticationService {
    // Passphrase which will be used for new sessions. Existing sessions will use the passphrase
    // stored in the SessionData.
    private val pendingPassphrase = getDatabasePassphrase()

    // Need to keep a copy of the current session path to eventually delete it.
    // Ideally it would be possible to get the sessionPath from the Client to avoid doing this.
    private var sessionPaths: SessionPaths? = null
    private var currentClient: Client? = null

    private val newMatrixClientObservers = mutableListOf<(MatrixClient) -> Unit>()
    override fun listenToNewMatrixClients(lambda: (MatrixClient) -> Unit) {
        newMatrixClientObservers.add(lambda)
    }

    private fun rotateSessionPath(): SessionPaths {
        sessionPaths?.deleteRecursively()
        return sessionPathsFactory.create()
            .also { sessionPaths = it }
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient> = withContext(coroutineDispatchers.io) {
        runCatchingExceptions {
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

    override suspend fun setHomeserver(homeserver: String): Result<MatrixHomeServerDetails> =
        withContext(coroutineDispatchers.io) {
            val emptySessionPath = rotateSessionPath()
            runCatchingExceptions {
                val client = makeClient(sessionPaths = emptySessionPath) {
                    serverNameOrHomeserverUrl(homeserver)
                }

                currentClient = client
                client.homeserverLoginDetails().map()
            }.onFailure {
                clear()
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to set homeserver to $homeserver")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun login(username: String, password: String): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.login(username, password, "Element X Android", null)
                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.PASSWORD,
                        passphrase = pendingPassphrase,
                        sessionPaths = currentSessionPaths,
                    )
                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.addSession(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to login")
                failure.mapAuthenticationException()
            }
        }

    override suspend fun importCreatedSession(externalSession: ExternalSession): Result<SessionId> =
        withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                val sessionData = externalSession.toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.PASSWORD,
                    passphrase = pendingPassphrase,
                    sessionPaths = currentSessionPaths,
                )
                clear()
                sessionStore.addSession(sessionData)
                SessionId(sessionData.userId)
            }
        }

    private var pendingOAuthAuthorizationData: OAuthAuthorizationData? = null

    override suspend fun getOidcUrl(
        prompt: OidcPrompt,
        loginHint: String?,
    ): Result<OidcDetails> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val oAuthAuthorizationData = client.urlForOidc(
                    oidcConfiguration = oidcConfigurationProvider.get(),
                    prompt = prompt.toRustPrompt(),
                    loginHint = loginHint,
                    // If we want to restore a previous session for which we have encryption keys, we can pass the deviceId here. At the moment, we don't
                    deviceId = null,
                    additionalScopes = emptyList(),
                )
                val url = oAuthAuthorizationData.loginUrl()
                pendingOAuthAuthorizationData = oAuthAuthorizationData
                OidcDetails(url)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to get OIDC URL")
                failure.mapAuthenticationException()
            }
        }
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                pendingOAuthAuthorizationData?.use {
                    currentClient?.abortOidcAuth(it)
                }
                pendingOAuthAuthorizationData = null
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to cancel OIDC login")
                failure.mapAuthenticationException()
            }
        }
    }

    /**
     * callbackUrl should be the uriRedirect from OidcClientMetadata (with all the parameters).
     */
    override suspend fun loginWithOidc(callbackUrl: String): Result<SessionId> {
        return withContext(coroutineDispatchers.io) {
            runCatchingExceptions {
                val client = currentClient ?: error("You need to call `setHomeserver()` first")
                val currentSessionPaths = sessionPaths ?: error("You need to call `setHomeserver()` first")
                client.loginWithOidcCallback(callbackUrl)

                // Free the pending data since we won't use it to abort the flow anymore
                pendingOAuthAuthorizationData?.close()
                pendingOAuthAuthorizationData = null

                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                val sessionData = client.session().toSessionData(
                    isTokenValid = true,
                    loginType = LoginType.OIDC,
                    passphrase = pendingPassphrase,
                    sessionPaths = currentSessionPaths,
                )
                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.addSession(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
            }.mapFailure { failure ->
                Timber.e(failure, "Failed to login with OIDC")
                failure.mapAuthenticationException()
            }
        }
    }

    @Throws(AuthenticationException.AccountAlreadyLoggedIn::class)
    private suspend fun ensureNotAlreadyLoggedIn(client: Client) {
        val newUserId = client.userId()
        val accountAlreadyLoggedIn = sessionStore.getAllSessions().any {
            it.userId == newUserId
        }
        if (accountAlreadyLoggedIn) {
            // Sign out the client, ignoring any error
            runCatchingExceptions {
                client.logout()
            }
            throw AuthenticationException.AccountAlreadyLoggedIn(newUserId)
        }
    }

    override suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) =
        withContext(coroutineDispatchers.io) {
            val sdkQrCodeLoginData = (qrCodeData as SdkQrCodeLoginData).rustQrCodeData
            val emptySessionPaths = rotateSessionPath()
            val oidcConfiguration = oidcConfigurationProvider.get()
            val progressListener = object : QrLoginProgressListener {
                override fun onUpdate(state: QrLoginProgress) {
                    Timber.d("QR Code login progress: $state")
                    progress(state.toStep())
                }
            }
            runCatchingExceptions {
                val client = makeQrCodeLoginClient(
                    sessionPaths = emptySessionPaths,
                    qrCodeData = sdkQrCodeLoginData,
                )
                client.newLoginWithQrCodeHandler(
                    oidcConfiguration = oidcConfiguration,
                ).use {
                    it.scan(
                        qrCodeData = qrCodeData.rustQrCodeData,
                        progressListener = progressListener,
                    )
                }
                // Ensure that the user is not already logged in with the same account
                ensureNotAlreadyLoggedIn(client)
                val sessionData = client.session()
                    .toSessionData(
                        isTokenValid = true,
                        loginType = LoginType.QR,
                        passphrase = pendingPassphrase,
                        sessionPaths = emptySessionPaths,
                    )
                val matrixClient = rustMatrixClientFactory.create(client)
                newMatrixClientObservers.forEach { it.invoke(matrixClient) }
                sessionStore.addSession(sessionData)

                // Clean up the strong reference held here since it's no longer necessary
                currentClient = null

                SessionId(sessionData.userId)
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

    private suspend fun makeClient(
        sessionPaths: SessionPaths,
        config: suspend ClientBuilder.() -> ClientBuilder,
    ): Client {
        Timber.d("Creating client with simplified sliding sync")
        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                passphrase = pendingPassphrase,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
            )
            .config()
            .build()
    }

    private suspend fun makeQrCodeLoginClient(
        sessionPaths: SessionPaths,
        qrCodeData: QrCodeData,
    ): Client {
        Timber.d("Creating client for QR Code login with simplified sliding sync")
        return rustMatrixClientFactory
            .getBaseClientBuilder(
                sessionPaths = sessionPaths,
                passphrase = pendingPassphrase,
                slidingSyncType = ClientBuilderSlidingSync.Discovered,
            )
            .serverNameOrHomeserverUrl(qrCodeData.serverName()!!)
            .build()
    }

    private fun clear() {
        currentClient?.close()
        currentClient = null
    }
}
