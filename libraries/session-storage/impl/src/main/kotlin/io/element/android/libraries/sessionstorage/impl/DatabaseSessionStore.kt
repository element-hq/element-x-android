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

import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DatabaseSessionStore @Inject constructor(
    private val database: SessionDatabase,
) : SessionStore {

    override fun isLoggedIn(): Flow<Boolean> {
        return database.sessionDataQueries.selectFirst()
            .asFlow()
            .mapToOneOrNull()
            .map { it != null }
    }

    override suspend fun storeData(sessionData: SessionData) {
        database.sessionDataQueries.insertSessionData(sessionData.toDbModel())
    }

    override suspend fun getLatestSession(): SessionData? {
        return database.sessionDataQueries.selectFirst()
            .executeAsOneOrNull()
            ?.toApiModel()
    }

    override suspend fun getSession(sessionId: String): SessionData? {
        return database.sessionDataQueries.selectByUserId(sessionId)
            .executeAsOneOrNull()
            ?.toApiModel()
    }

    override suspend fun getAllSessions(): List<SessionData> {
        return database.sessionDataQueries.selectAll()
            .executeAsList()
            .map { it.toApiModel() }
    }

    override fun sessionsFlow(): Flow<List<SessionData>> {
        Timber.w("Observing session list!")
        return database.sessionDataQueries.selectAll()
            .asFlow()
            .mapToList()
            .map { it.map { sessionData -> sessionData.toApiModel() } }
    }

    override suspend fun removeSession(sessionId: String) {
        database.sessionDataQueries.removeSession(sessionId)
    }
}
