/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.matrix.session

import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.Database
import io.element.android.libraries.matrix.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.matrix.rustcomponents.sdk.Session
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PreferencesSessionStore @Inject constructor(
    private val database: Database,
) : SessionStore {

    override fun isLoggedIn(): Flow<Boolean> {
        return database.sessionDataQueries.selectFirst().asFlow().mapToOneOrNull().map { it != null }
    }

    override suspend fun storeData(session: Session) {
        val sessionData = SessionData(
            accessToken = session.accessToken,
            deviceId = session.deviceId,
            homeserverUrl = session.homeserverUrl,
            isSoftLogout = session.isSoftLogout,
            refreshToken = session.refreshToken,
            userId = session.userId
        )
        database.sessionDataQueries.insertSessionData(sessionData)
    }

    override suspend fun getLatestSession(): Session? {
        return database.sessionDataQueries.selectFirst()
            .executeAsOneOrNull()?.let { sessionData ->
                Session(
                    accessToken = sessionData.accessToken,
                    deviceId = sessionData.deviceId,
                    homeserverUrl = sessionData.homeserverUrl,
                    isSoftLogout = sessionData.isSoftLogout,
                    refreshToken = sessionData.refreshToken,
                    userId = sessionData.userId
                )
        }
    }

    override suspend fun getSession(sessionId: SessionId): Session? {
        //TODO we should have a proper session management
        return getLatestSession()
    }

    override suspend fun reset() {
        database.sessionDataQueries.removeAll()
    }
}
