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

package io.element.android.libraries.sessionstorage.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DatabaseSessionStore @Inject constructor(
    private val database: SessionDatabase,
    private val dispatchers: CoroutineDispatchers,
) : SessionStore {
    private val sessionDataMutex = Mutex()

    override fun isLoggedIn(): Flow<LoggedInState> {
        return database.sessionDataQueries.selectFirst()
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
    }

    override suspend fun storeData(sessionData: SessionData) = sessionDataMutex.withLock {
        database.sessionDataQueries.insertSessionData(sessionData.toDbModel())
    }

    override suspend fun updateData(sessionData: SessionData) = sessionDataMutex.withLock {
        val result = database.sessionDataQueries.selectByUserId(sessionData.userId)
            .executeAsOneOrNull()
            ?.toApiModel()

        if (result == null) {
            Timber.e("User ${sessionData.userId} not found in session database")
            return
        }
            // Copy new data from SDK, but keep login timestamp
        database.sessionDataQueries.updateSession(
            sessionData.copy(
                loginTimestamp = result.loginTimestamp,
            ).toDbModel()
        )
    }

    override suspend fun getLatestSession(): SessionData? {
        return sessionDataMutex.withLock {
            database.sessionDataQueries.selectFirst()
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

    override fun sessionsFlow(): Flow<List<SessionData>> {
        Timber.w("Observing session list!")
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
