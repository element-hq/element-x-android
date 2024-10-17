/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.LoggedInState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MatrixAuthenticationService {
    fun loggedInStateFlow(): Flow<LoggedInState>
    suspend fun getLatestSessionId(): SessionId?

    /**
     * Restore a session from a [sessionId].
     * Do not restore anything it the access token is not valid anymore.
     * Generally this method should not be used directly, prefer using [MatrixClientProvider.getOrRestore] instead.
     */
    suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient>
    fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?>
    suspend fun setHomeserver(homeserver: String): Result<Unit>
    suspend fun login(username: String, password: String): Result<SessionId>

    /**
     * Import a session that was created using another client, for instance Element Web.
     */
    suspend fun importCreatedSession(externalSession: ExternalSession): Result<SessionId>

    /*
     * OIDC part.
     */

    /**
     * Get the Oidc url to display to the user.
     */
    suspend fun getOidcUrl(prompt: OidcPrompt): Result<OidcDetails>

    /**
     * Cancel Oidc login sequence.
     */
    suspend fun cancelOidcLogin(): Result<Unit>

    /**
     * Attempt to login using the [callbackUrl] provided by the Oidc page.
     */
    suspend fun loginWithOidc(callbackUrl: String): Result<SessionId>

    suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId>
}
