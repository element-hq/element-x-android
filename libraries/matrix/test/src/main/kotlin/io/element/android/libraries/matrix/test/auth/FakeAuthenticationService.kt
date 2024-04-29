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

package io.element.android.libraries.matrix.test.auth

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.auth.MatrixHomeServerDetails
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

val A_OIDC_DATA = OidcDetails(url = "a-url")

class FakeAuthenticationService(
    var loginWithQrCodeResult: (qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit) -> Result<SessionId> =
        lambdaRecorder<MatrixQrCodeLoginData, (QrCodeLoginStep) -> Unit, Result<SessionId>> { _, _ -> Result.success(A_SESSION_ID) },
) : MatrixAuthenticationService {
    private val homeserver = MutableStateFlow<MatrixHomeServerDetails?>(null)
    private var oidcError: Throwable? = null
    private var oidcCancelError: Throwable? = null
    private var loginError: Throwable? = null
    private var changeServerError: Throwable? = null
    private var matrixClient: MatrixClient? = null

    override fun loggedInStateFlow(): Flow<LoggedInState> {
        return flowOf(LoggedInState.NotLoggedIn)
    }

    override suspend fun getLatestSessionId(): SessionId? {
        return null
    }

    override suspend fun restoreSession(sessionId: SessionId): Result<MatrixClient> {
        return if (matrixClient != null) {
            Result.success(matrixClient!!)
        } else {
            Result.failure(IllegalStateException())
        }
    }

    override fun getHomeserverDetails(): StateFlow<MatrixHomeServerDetails?> {
        return homeserver
    }

    fun givenHomeserver(homeserver: MatrixHomeServerDetails) {
        this.homeserver.value = homeserver
    }

    override suspend fun setHomeserver(homeserver: String): Result<Unit> = simulateLongTask {
        changeServerError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun login(username: String, password: String): Result<SessionId> = simulateLongTask {
        loginError?.let { Result.failure(it) } ?: Result.success(A_USER_ID)
    }

    override suspend fun getOidcUrl(): Result<OidcDetails> = simulateLongTask {
        oidcError?.let { Result.failure(it) } ?: Result.success(A_OIDC_DATA)
    }

    override suspend fun cancelOidcLogin(): Result<Unit> {
        return oidcCancelError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun loginWithOidc(callbackUrl: String): Result<SessionId> = simulateLongTask {
        loginError?.let { Result.failure(it) } ?: Result.success(A_USER_ID)
    }

    override suspend fun loginWithQrCode(qrCodeData: MatrixQrCodeLoginData, progress: (QrCodeLoginStep) -> Unit): Result<SessionId> = simulateLongTask {
        loginWithQrCodeResult(qrCodeData, progress)
    }

    fun givenOidcError(throwable: Throwable?) {
        oidcError = throwable
    }

    fun givenOidcCancelError(throwable: Throwable?) {
        oidcCancelError = throwable
    }

    fun givenLoginError(throwable: Throwable?) {
        loginError = throwable
    }

    fun givenChangeServerError(throwable: Throwable?) {
        changeServerError = throwable
    }

    fun givenMatrixClient(matrixClient: MatrixClient) {
        this.matrixClient = matrixClient
    }
}
