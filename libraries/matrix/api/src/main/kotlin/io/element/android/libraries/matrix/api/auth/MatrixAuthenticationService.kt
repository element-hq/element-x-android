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
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Creates and restores sessions: password login, OAuth login, QR code login, and importing a session from another client.
 *
 * [setHomeserver] must be called before any login attempt, since it is what tells the SDK which server to authenticate against.
 * Every successful login stores the session and notifies the listeners registered through [listenToNewMatrixClients].
 */
interface MatrixAuthenticationService {
    /**
     * Restore a session from a [sessionId].
     * Do not restore anything it the access token is not valid anymore.
     * Generally this method should not be used directly, prefer using [MatrixClientProvider.getOrRestore] instead.
     *
     * @param sessionId the session to restore.
     */
    suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient>

    /**
     * Set the homeserver to use for authentication, and return its details.
     *
     * @param homeserver the homeserver URL or server name to authenticate against.
     */
    suspend fun setHomeserver(homeserver: String): Result<MatrixHomeServerDetails>

    /**
     * Logs in with a password on the homeserver previously set through [setHomeserver].
     *
     * @param username the user id or its local part.
     * @param password the account password.
     */
    suspend fun login(username: String, password: String): Result<SessionId>

    /*
     * OAuth part.
     */

    /**
     * Get the OAuth url to display to the user.
     *
     * @param prompt whether the authentication server should show the sign-in or the account-creation page.
     * @param loginHint the user identifier to pre-fill on that page, or `null` to leave it empty.
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
     *
     * @param session the session found in a local Element Classic install, or `null` when there is none to migrate.
     */
    fun setElementClassicSession(session: ElementClassicSession?)

    /**
     * Check if the provided secrets from Element Classic session contain a key backup.
     *
     * @param userId the user the secrets belong to.
     * @param secrets the raw secrets exported from Element Classic.
     * @param backupInfo the raw backup description exported alongside them.
     */
    fun doSecretsContainBackupKey(
        userId: UserId,
        secrets: String,
        backupInfo: String,
    ): Boolean

    /**
     * Attempt to log in using the [callbackUrl] provided by the OAuth page.
     *
     * @param callbackUrl the redirect URL the authentication server sent the user back to, carrying the authorization code.
     */
    suspend fun loginWithOAuth(callbackUrl: String): Result<SessionId>

    /**
     * Logs in by acting on a QR code shown by an already signed in device, reporting each step of the exchange as it happens.
     *
     * @param qrCodeData the decoded QR code, obtained from [io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory].
     * @param progress called on every step of the flow, so the UI can follow along.
     */
    suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId>

    /**
     * Listen to new Matrix clients being created on authentication.
     *
     * @param lambda called with each client created by a successful login or session restore.
     */
    fun listenToNewMatrixClients(lambda: (MatrixClient) -> Unit)
}
