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

package io.element.android.samples.minimal

import io.element.android.libraries.matrix.session.SessionData
import io.element.android.libraries.sessionstorage.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySessionStore : SessionStore {

    private var sessionData = MutableStateFlow<SessionData?>(null)

    override fun isLoggedIn(): Flow<Boolean> {
        return sessionData.map { it != null }
    }

    override suspend fun storeData(session: SessionData) {
        sessionData.value = session
    }

    override suspend fun getSession(sessionId: String): SessionData? {
        return sessionData.value.takeIf { it?.userId == sessionId }
    }

    override suspend fun getLatestSession(): SessionData? {
        return sessionData.value
    }

    override suspend fun removeSession(sessionId: String) {
        if (sessionData.value?.userId == sessionId) {
            sessionData.value = null
        }
    }
}
