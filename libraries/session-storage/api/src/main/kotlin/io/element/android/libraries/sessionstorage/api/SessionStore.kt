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

package io.element.android.libraries.sessionstorage.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SessionStore {
    fun isLoggedIn(): Flow<LoggedInState>
    fun sessionsFlow(): Flow<List<SessionData>>
    suspend fun storeData(sessionData: SessionData)

    /**
     * Will update the session data matching the userId, except the value of loginTimestamp.
     * No op if userId is not found in DB.
     */
    suspend fun updateData(sessionData: SessionData)
    suspend fun getSession(sessionId: String): SessionData?
    suspend fun getAllSessions(): List<SessionData>
    suspend fun getLatestSession(): SessionData?
    suspend fun removeSession(sessionId: String)
}

fun List<SessionData>.toUserList(): List<String> {
    return map { it.userId }
}

fun Flow<List<SessionData>>.toUserListFlow(): Flow<List<String>> {
    return map { it.toUserList() }
}
