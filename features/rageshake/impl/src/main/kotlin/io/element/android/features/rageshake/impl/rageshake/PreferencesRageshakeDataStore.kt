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

package io.element.android.features.rageshake.impl.rageshake

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.rageshake.RageshakeDataStore
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_rageshake")

private val enabledKey = booleanPreferencesKey("enabled")
private val sensitivityKey = floatPreferencesKey("sensitivity")

@ContributesBinding(AppScope::class)
class PreferencesRageshakeDataStore @Inject constructor(
    @ApplicationContext context: Context
) : RageshakeDataStore {
    private val store = context.dataStore

    override fun isEnabled(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[enabledKey].orTrue()
        }
    }

    override suspend fun setIsEnabled(isEnabled: Boolean) {
        store.edit { prefs ->
            prefs[enabledKey] = isEnabled
        }
    }

    override fun sensitivity(): Flow<Float> {
        return store.data.map { prefs ->
            prefs[sensitivityKey] ?: 0.5f
        }
    }

    override suspend fun setSensitivity(sensitivity: Float) {
        store.edit { prefs ->
            prefs[sensitivityKey] = sensitivity
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}
