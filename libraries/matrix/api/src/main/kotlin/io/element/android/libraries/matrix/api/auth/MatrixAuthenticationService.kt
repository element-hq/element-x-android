/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId

interface MatrixAuthenticationService {
    /**
     * Restore a session from a [sessionId].
     * Do not restore anything it the access token is not valid anymore.
     * Generally this method should not be used directly, prefer using [MatrixClientProvider.getOrRestore] instead.
     */
    suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient>

    /**
     * Set the homeserver to use for authentication, and return its details.
     */
    suspend fun setHomeserver(homeserver: String): Result<MatrixHomeServerDetails>

    suspend fun login(username: String, password: String): Result<SessionId>

    /**
     * Import a session that was created using another client, for instance Element Web.
     */
    suspend fun importCreatedSession(externalSession: ExternalSession): Result<SessionId>

    /*
     * OAuth part.
     */

    /**
     * Get the OAuth url to display to the user.
     */
    suspend fun getOAuthUrl(
        prompt: OAuthPrompt,
        loginHint: String?,
    ): Result<OAuthDetails>

    /**
     * Cancel OAuth login sequence.
     */
    suspend fun cancelOAuthLogin(): Result<Unit>

    /**
     * Set the existing data about Element Classic session, if any.
     */
    fun setElementClassicSession(session: ElementClassicSession?)

    /**
     * Check if the provided secrets from Element Classic session contain a key backup.
     */
    fun doSecretsContainBackupKey(
        userId: UserId,
        secrets: String,
        backupInfo: String,
    ): Boolean

    /**
     * Attempt to log in using the [callbackUrl] provided by the OAuth page.
     */
    suspend fun loginWithOAuth(callbackUrl: String): Result<SessionId>

    suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId>

    /** Listen to new Matrix clients being created on authentication. */
    fun listenToNewMatrixClients(lambda: (MatrixClient) -> Unit)
}
