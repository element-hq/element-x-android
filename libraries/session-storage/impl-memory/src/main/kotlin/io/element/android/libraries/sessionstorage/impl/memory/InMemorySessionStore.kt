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

package io.element.android.libraries.sessionstorage.impl.memory

import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySessionStore : SessionStore {
    private var sessionDataFlow = MutableStateFlow<SessionData?>(null)

    override fun isLoggedIn(): Flow<LoggedInState> {
        return sessionDataFlow.map {
            if (it == null) {
                LoggedInState.NotLoggedIn
            } else {
                LoggedInState.LoggedIn(
                    sessionId = it.userId,
                    isTokenValid = it.isTokenValid,
                )
            }
        }
    }

    override fun sessionsFlow(): Flow<List<SessionData>> {
        return sessionDataFlow.map { listOfNotNull(it) }
    }

    override suspend fun storeData(sessionData: SessionData) {
        sessionDataFlow.value = sessionData
    }

    override suspend fun updateData(sessionData: SessionData) {
        sessionDataFlow.value = sessionData
    }

    override suspend fun getSession(sessionId: String): SessionData? {
        return sessionDataFlow.value.takeIf { it?.userId == sessionId }
    }

    override suspend fun getAllSessions(): List<SessionData> {
        return listOfNotNull(sessionDataFlow.value)
    }

    override suspend fun getLatestSession(): SessionData? {
        return sessionDataFlow.value
    }

    override suspend fun removeSession(sessionId: String) {
        if (sessionDataFlow.value?.userId == sessionId) {
            sessionDataFlow.value = null
        }
    }
}
