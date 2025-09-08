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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemorySessionStore(
    initialList: List<SessionData> = emptyList(),
) : SessionStore {
    private val sessionDataListFlow = MutableStateFlow(initialList)

    override fun isLoggedIn(): Flow<LoggedInState> {
        return sessionDataListFlow.map {
            if (it.isEmpty()) {
                LoggedInState.NotLoggedIn
            } else {
                it.first().let { sessionData ->
                    LoggedInState.LoggedIn(
                        sessionId = sessionData.userId,
                        isTokenValid = sessionData.isTokenValid,
                    )
                }
            }
        }
    }

    override fun sessionsFlow(): Flow<List<SessionData>> = sessionDataListFlow.asStateFlow()

    override suspend fun storeData(sessionData: SessionData) {
        val currentList = sessionDataListFlow.value.toMutableList()
        currentList.removeAll { it.userId == sessionData.userId }
        currentList.add(sessionData)
        sessionDataListFlow.value = currentList
    }

    override suspend fun updateData(sessionData: SessionData) {
        val currentList = sessionDataListFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.userId == sessionData.userId }
        if (index != -1) {
            currentList[index] = sessionData
            sessionDataListFlow.value = currentList
        }
    }

    override suspend fun getSession(sessionId: String): SessionData? {
        return sessionDataListFlow.value.firstOrNull { it.userId == sessionId }
    }

    override suspend fun getAllSessions(): List<SessionData> {
        return sessionDataListFlow.value
    }

    override suspend fun getLatestSession(): SessionData? {
        return sessionDataListFlow.value.firstOrNull()
    }

    override suspend fun removeSession(sessionId: String) {
        val currentList = sessionDataListFlow.value.toMutableList()
        currentList.removeAll { it.userId == sessionId }
        sessionDataListFlow.value = currentList
    }
}
