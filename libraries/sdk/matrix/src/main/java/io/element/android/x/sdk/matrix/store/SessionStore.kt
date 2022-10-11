package io.element.android.x.sdk.matrix.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_sessions")

internal class SessionStore(
    context: Context
) {
    data class SessionData(
        val userId: String,
        val restoreToken: String,
    )

    private val store = context.dataStore

    private val userIdPreference = stringPreferencesKey("userId")
    private val restoreTokenPreference = stringPreferencesKey("restoreToken")

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