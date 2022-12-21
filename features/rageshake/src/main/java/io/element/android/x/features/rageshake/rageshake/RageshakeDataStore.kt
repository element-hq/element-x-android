package io.element.android.x.features.rageshake.rageshake

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.element.android.x.core.bool.orTrue
import io.element.android.x.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_rageshake")

private val enabledKey = booleanPreferencesKey("enabled")
private val sensitivityKey = floatPreferencesKey("sensitivity")

class RageshakeDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.dataStore

    fun isEnabled(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[enabledKey].orTrue()
        }
    }

    suspend fun setIsEnabled(isEnabled: Boolean) {
        store.edit { prefs ->
            prefs[enabledKey] = isEnabled
        }
    }

    fun sensitivity(): Flow<Float> {
        return store.data.map { prefs ->
            prefs[sensitivityKey] ?: 0.5f
        }
    }

    suspend fun setSensitivity(sensitivity: Float) {
        store.edit { prefs ->
            prefs[sensitivityKey] = sensitivity
        }
    }

    suspend fun reset() {
        store.edit { it.clear() }
    }
}
