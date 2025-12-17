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
     * OIDC part.
     */

    /**
     * Get the Oidc url to display to the user.
     */
    suspend fun getOidcUrl(
        prompt: OidcPrompt,
        loginHint: String?,
    ): Result<OidcDetails>

    /**
     * Cancel Oidc login sequence.
     */
    suspend fun cancelOidcLogin(): Result<Unit>

    /**
     * Attempt to login using the [callbackUrl] provided by the Oidc page.
     */
    suspend fun loginWithOidc(callbackUrl: String): Result<SessionId>

    suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId>

    /** Listen to new Matrix clients being created on authentication. */
    fun listenToNewMatrixClients(lambda: (MatrixClient) -> Unit)
}
