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

package io.element.android.x.matrix.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.x.di.AppScope
import io.element.android.x.di.ApplicationContext
import io.element.android.x.di.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.matrix.rustcomponents.sdk.Session
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_sessions")

// TODO It contains the access token, so it has to be stored in a more secured storage.
private val sessionKey = stringPreferencesKey("session")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PreferencesSessionStore @Inject constructor(
    @ApplicationContext context: Context
) : SessionStore {
    @Serializable
    data class SessionData(
        val accessToken: String,
        val deviceId: String,
        val homeserverUrl: String,
        val isSoftLogout: Boolean,
        val refreshToken: String?,
        val userId: String
    )

    private val store = context.dataStore

    override fun isLoggedIn(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[sessionKey] != null
        }
    }

    override suspend fun storeData(session: Session) {
        store.edit { prefs ->
            val sessionData = SessionData(
                accessToken = session.accessToken,
                deviceId = session.deviceId,
                homeserverUrl = session.homeserverUrl,
                isSoftLogout = session.isSoftLogout,
                refreshToken = session.refreshToken,
                userId = session.userId
            )
            val encodedSession = Json.encodeToString(sessionData)
            prefs[sessionKey] = encodedSession
        }
    }

    override suspend fun getLatestSession(): Session? {
        return store.data.firstOrNull()?.let { prefs ->
            val encodedSession = prefs[sessionKey] ?: return@let null
            val sessionData = Json.decodeFromString<SessionData>(encodedSession)
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

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}
