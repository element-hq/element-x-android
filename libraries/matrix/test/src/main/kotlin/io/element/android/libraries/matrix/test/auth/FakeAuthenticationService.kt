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
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_USER_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthenticationService : MatrixAuthenticationService {
    private var homeserver: MatrixHomeServerDetails = A_HOMESERVER
    private var loginError: Throwable? = null
    private var changeServerError: Throwable? = null

    override fun isLoggedIn(): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun getLatestSessionId(): SessionId? {
        return null
    }

    override suspend fun restoreSession(sessionId: SessionId): MatrixClient? {
        return null
    }

    override fun getHomeserver(): MatrixHomeServerDetails? {
        return null
    }

    fun givenHomeserver(homeserver: MatrixHomeServerDetails) {
        this.homeserver = homeserver
    }

    override suspend fun setHomeserver(homeserver: String) {
        changeServerError?.let { throw it }
        delay(100)
    }

    override suspend fun login(username: String, password: String): SessionId {
        delay(100)
        loginError?.let { throw it }
        return A_USER_ID
    }

    fun givenLoginError(throwable: Throwable?) {
        loginError = throwable
    }

    fun givenChangeServerError(throwable: Throwable?) {
        changeServerError = throwable
    }
}
