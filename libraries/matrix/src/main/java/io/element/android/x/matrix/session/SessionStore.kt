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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_sessions")
private val userIdPreference = stringPreferencesKey("userId")

// TODO It contains the access token, so it has to be stored in a more secured storage.
// I would expect the Rust SDK to provide a more obscure token.
private val restoreTokenPreference = stringPreferencesKey("restoreToken")


internal class SessionStore(
    context: Context
) {
    data class SessionData(
        val userId: String,
        val restoreToken: String,
    )

    private val store = context.dataStore

    fun isLoggedIn(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[userIdPreference] != null && prefs[restoreTokenPreference] != null
        }
    }

    suspend fun storeData(sessionData: SessionData) {
        store.edit { prefs ->
            prefs[userIdPreference] = sessionData.userId
            prefs[restoreTokenPreference] = sessionData.restoreToken
        }
    }

    suspend fun getStoredData(): SessionData? {
        return store.data.firstOrNull()?.let { prefs ->
            val userId = prefs[userIdPreference] ?: return@let null
            val restoreToken = prefs[restoreTokenPreference] ?: return@let null
            SessionData(
                userId = userId,
                restoreToken = restoreToken,
            )
        }
    }

    suspend fun reset() {
        store.edit { it.clear() }
    }
}