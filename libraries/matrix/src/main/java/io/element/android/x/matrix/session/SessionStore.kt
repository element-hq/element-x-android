package io.element.android.x.matrix.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.matrix.rustcomponents.sdk.Session

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_sessions")

// TODO It contains the access token, so it has to be stored in a more secured storage.
private val sessionKey = stringPreferencesKey("session")

internal class SessionStore(
    context: Context
) {
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

    fun isLoggedIn(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[sessionKey] != null
        }
    }

    suspend fun storeData(session: Session) {
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

    suspend fun getLatestSession(): Session? {
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

    suspend fun reset() {
        store.edit { it.clear() }
    }
}