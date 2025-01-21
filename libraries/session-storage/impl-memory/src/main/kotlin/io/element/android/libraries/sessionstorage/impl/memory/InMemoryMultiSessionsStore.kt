/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
