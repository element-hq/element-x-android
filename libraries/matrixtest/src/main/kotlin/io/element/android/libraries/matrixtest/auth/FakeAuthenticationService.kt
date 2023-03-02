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

package io.element.android.libraries.matrixtest.auth

import io.element.android.libraries.matrix.MatrixClient
import io.element.android.libraries.matrix.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.core.UserId
import io.element.android.libraries.matrixtest.A_HOMESERVER
import io.element.android.libraries.matrixtest.A_USER_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthenticationService : MatrixAuthenticationService {
    private var homeserver: String = A_HOMESERVER
    private var loginError: Throwable? = null

    override fun isLoggedIn(): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun getLatestSessionId(): UserId? {
        return null
    }

    override suspend fun restoreSession(userId: UserId): MatrixClient? {
        return null
    }

    override fun getHomeserver(): String? {
        return null
    }

    fun givenHomeserver(homeserver: String) {
        this.homeserver = homeserver
    }

    override fun getHomeserverOrDefault(): String {
        return homeserver
    }

    override suspend fun setHomeserver(homeserver: String) {
        delay(100)
    }

    override suspend fun login(username: String, password: String): UserId {
        delay(100)
        loginError?.let { throw it }
        return A_USER_ID
    }

    fun givenLoginError(throwable: Throwable?) {
        loginError = throwable
    }
}
