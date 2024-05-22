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

class InMemoryMultiSessionsStore : SessionStore {
    private val sessions = mutableListOf<SessionData>()

    override fun isLoggedIn(): Flow<LoggedInState> = error("Not implemented")

    override fun sessionsFlow(): Flow<List<SessionData>> = error("Not implemented")

    override suspend fun storeData(sessionData: SessionData) {
        sessions.add(sessionData)
    }

    override suspend fun updateData(sessionData: SessionData) = error("Not implemented")

    override suspend fun getSession(sessionId: String): SessionData? = error("Not implemented")

    override suspend fun getAllSessions(): List<SessionData> = sessions

    override suspend fun getLatestSession(): SessionData = error("Not implemented")

    override suspend fun removeSession(sessionId: String) = error("Not implemented")
}
