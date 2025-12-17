/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DatabaseSessionStore(
    private val database: SessionDatabase,
    private val dispatchers: CoroutineDispatchers,
) : SessionStore {
    private val sessionDataMutex = Mutex()

    override fun loggedInStateFlow(): Flow<LoggedInState> {
        return database.sessionDataQueries.selectLatest()
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map {
                if (it == null) {
                    LoggedInState.NotLoggedIn
                } else {
                    LoggedInState.LoggedIn(
                        sessionId = it.userId,
                        isTokenValid = it.isTokenValid == 1L
                    )
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun addSession(sessionData: SessionData) {
        sessionDataMutex.withLock {
            val lastUsageIndex = getLastUsageIndex()
            database.sessionDataQueries.insertSessionData(
                sessionData
                    .copy(
                        // position value does not really matter, so just use lastUsageIndex + 1 to ensure that
                        // the value is always greater than value of any existing account
                        position = lastUsageIndex + 1,
                        lastUsageIndex = lastUsageIndex + 1,
                    )
                    .toDbModel()
            )
        }
    }

    override suspend fun updateData(sessionData: SessionData) {
        sessionDataMutex.withLock {
            val result = database.sessionDataQueries.selectByUserId(sessionData.userId)
                .executeAsOneOrNull()
                ?.toApiModel()

            if (result == null) {
                Timber.e("User ${sessionData.userId} not found in session database")
                return
            }
            // Copy new data from SDK, but keep application data
            database.sessionDataQueries.updateSession(
                sessionData.copy(
                    loginTimestamp = result.loginTimestamp,
                    position = result.position,
                    lastUsageIndex = result.lastUsageIndex,
                    userDisplayName = result.userDisplayName,
                    userAvatarUrl = result.userAvatarUrl,
                ).toDbModel()
            )
        }
    }

    override suspend fun updateUserProfile(sessionId: String, displayName: String?, avatarUrl: String?) {
        sessionDataMutex.withLock {
            val result = database.sessionDataQueries.selectByUserId(sessionId)
                .executeAsOneOrNull()
                ?.toApiModel()
            if (result == null) {
                Timber.e("User $sessionId not found in session database")
                return
            }
            database.sessionDataQueries.updateSession(
                result.copy(
                    userDisplayName = displayName,
                    userAvatarUrl = avatarUrl,
                ).toDbModel()
            )
        }
    }

    override suspend fun setLatestSession(sessionId: String) {
        val latestSession = getLatestSession()
        if (latestSession?.userId == sessionId) {
            // Already the latest session
            return
        }
        val lastUsageIndex = latestSession?.lastUsageIndex ?: 0
        val result = database.sessionDataQueries.selectByUserId(sessionId)
            .executeAsOneOrNull()
            ?.toApiModel()
        if (result == null) {
            Timber.e("User $sessionId not found in session database")
            return
        }
        sessionDataMutex.withLock {
            // Update lastUsageIndex of the session
            database.sessionDataQueries.updateSession(
                result.copy(
                    lastUsageIndex = lastUsageIndex + 1,
                ).toDbModel()
            )
        }
    }

    private fun getLastUsageIndex(): Long {
        return database.sessionDataQueries.selectLatest()
            .executeAsOneOrNull()
            ?.lastUsageIndex
            ?: -1L
    }

    override suspend fun getLatestSession(): SessionData? {
        return sessionDataMutex.withLock {
            database.sessionDataQueries.selectLatest()
                .executeAsOneOrNull()
                ?.toApiModel()
        }
    }

    override suspend fun getSession(sessionId: String): SessionData? {
        return sessionDataMutex.withLock {
            database.sessionDataQueries.selectByUserId(sessionId)
                .executeAsOneOrNull()
                ?.toApiModel()
        }
    }

    override suspend fun getAllSessions(): List<SessionData> {
        return sessionDataMutex.withLock {
            database.sessionDataQueries.selectAll()
                .executeAsList()
                .map { it.toApiModel() }
        }
    }

    override suspend fun numberOfSessions(): Int {
        return sessionDataMutex.withLock {
            database.sessionDataQueries.count()
                .executeAsOneOrNull()
                ?.toInt()
                ?: 0
        }
    }

    override fun sessionsFlow(): Flow<List<SessionData>> {
        return database.sessionDataQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { it.map { sessionData -> sessionData.toApiModel() } }
    }

    override suspend fun removeSession(sessionId: String) {
        sessionDataMutex.withLock {
            database.sessionDataQueries.removeSession(sessionId)
        }
    }
}
