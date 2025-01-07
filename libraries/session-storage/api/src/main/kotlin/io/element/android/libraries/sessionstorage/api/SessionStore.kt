/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
