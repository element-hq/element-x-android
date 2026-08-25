/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the logged in sessions and their access tokens, so the app can restore them after a restart.
 *
 * Sessions are ordered by last usage, which is what makes "the latest session" meaningful across the interface.
 */
interface SessionStore {
    /**
     * A flow emitting the current logged in state.
     * If there is at least one session, the state is [LoggedInState.LoggedIn] with the latest used session.
     * If there is no session, the state is [LoggedInState.NotLoggedIn].
     */
    fun loggedInStateFlow(): Flow<LoggedInState>

    /**
     * Return a flow of all sessions ordered by last usage descending.
     */
    fun sessionsFlow(): Flow<List<SessionData>>

    /**
     * Add a new session. If other sessions exist, the new one will be set as the latest used one, and
     * the added session position will be set to a value higher than the other session positions.
     *
     * @param sessionData the session to store, including its tokens.
     */
    suspend fun addSession(sessionData: SessionData)

    /**
     * Will update the session data matching the userId, except the value of loginTimestamp.
     * No op if userId is not found in DB.
     *
     * @param sessionData the new content of the session.
     */
    suspend fun updateData(sessionData: SessionData)

    /**
     * Update the user profile info of the session matching the userId.
     *
     * @param sessionId the session to update.
     * @param displayName the new display name, or `null` when the user has none.
     * @param avatarUrl the new avatar, or `null` when the user has none.
     */
    suspend fun updateUserProfile(sessionId: String, displayName: String?, avatarUrl: String?)

    /**
     * Get the session data matching the userId, or null if not found.
     *
     * @param sessionId the session to read.
     */
    suspend fun getSession(sessionId: String): SessionData?

    /**
     * Get all sessions ordered by last usage descending.
     */
    suspend fun getAllSessions(): List<SessionData>

    /**
     * Get the number of sessions.
     */
    suspend fun numberOfSessions(): Int

    /**
     * Get the latest session, or null if no session exists.
     */
    suspend fun getLatestSession(): SessionData?

    /**
     * Set the session with [sessionId] as the latest used one.
     *
     * @param sessionId the session to bring to the front of the ordering.
     */
    suspend fun setLatestSession(sessionId: String)

    /**
     * Remove the session matching the sessionId.
     *
     * @param sessionId the session to forget, which also discards its tokens.
     */
    suspend fun removeSession(sessionId: String)
}

fun List<SessionData>.toUserList(): List<String> {
    return map { it.userId }
}

fun Flow<List<SessionData>>.toUserListFlow(): Flow<List<String>> {
    return map { it.toUserList() }
}

/**
 * @return a flow emitting the sessionId of the latest session if logged in, null otherwise.
 */
fun SessionStore.sessionIdFlow(): Flow<String?> {
    return loggedInStateFlow().map {
        when (it) {
            is LoggedInState.LoggedIn -> it.sessionId
            is LoggedInState.NotLoggedIn -> null
        }
    }
}
