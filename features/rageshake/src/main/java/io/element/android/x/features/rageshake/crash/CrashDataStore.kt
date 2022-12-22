package io.element.android.x.features.rageshake.crash

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.element.android.x.core.bool.orFalse
import io.element.android.x.di.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_crash")

private val appHasCrashedKey = booleanPreferencesKey("appHasCrashed")
private val crashDataKey = stringPreferencesKey("crashData")

class CrashDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.dataStore

    fun setCrashData(crashData: String) {
        // Must block
        runBlocking {
            store.edit { prefs ->
                prefs[appHasCrashedKey] = true
                prefs[crashDataKey] = crashData
            }
        }
    }

    suspend fun resetAppHasCrashed() {
        store.edit { prefs ->
            prefs[appHasCrashedKey] = false
        }
    }

    fun appHasCrashed(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[appHasCrashedKey].orFalse()
        }
    }

    fun crashInfo(): Flow<String> {
        return store.data.map { prefs ->
            prefs[crashDataKey].orEmpty()
        }
    }

    suspend fun reset() {
        store.edit { it.clear() }
    }
}
