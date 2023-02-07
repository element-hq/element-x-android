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
import io.element.android.libraries.matrix.core.SessionId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

const val A_HOMESERVER = "matrix.org"

class FakeAuthenticationService : MatrixAuthenticationService {
    override fun isLoggedIn(): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun getLatestSessionId(): SessionId? {
        return null
    }

    override suspend fun restoreSession(sessionId: SessionId): MatrixClient? {
        return null
    }

    override fun getHomeserver(): String? {
        return null
    }

    override fun getHomeserverOrDefault(): String {
        return A_HOMESERVER
    }

    override suspend fun setHomeserver(homeserver: String) {
        delay(100)
    }

    override suspend fun login(username: String, password: String): SessionId {
        return SessionId("test")
    }
}
