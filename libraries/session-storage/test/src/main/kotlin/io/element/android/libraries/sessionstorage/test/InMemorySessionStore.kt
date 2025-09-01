/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test

import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemorySessionStore(
    private val updateUserProfileResult: (String, String?, String?) -> Unit = { _, _, _ -> error("Not implemented") },
    private val setLatestSessionResult: (String) -> Unit = { error("Not implemented") },
) : SessionStore {
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

    override suspend fun addSession(sessionData: SessionData) {
        sessionDataFlow.value = sessionData
    }

    override suspend fun updateData(sessionData: SessionData) {
        sessionDataFlow.value = sessionData
    }

    override suspend fun updateUserProfile(sessionId: String, displayName: String?, avatarUrl: String?) {
        updateUserProfileResult(sessionId, displayName, avatarUrl)
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

    override suspend fun setLatestSession(sessionId: String) {
        setLatestSessionResult(sessionId)
    }

    override suspend fun removeSession(sessionId: String) {
        if (sessionDataFlow.value?.userId == sessionId) {
            sessionDataFlow.value = null
        }
    }
}
