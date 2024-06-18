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

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.MatrixClient
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

    /*
     * OIDC part.
     */

    /**
     * Get the Oidc url to display to the user.
     */
    suspend fun getOidcUrl(): Result<OidcDetails>

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
