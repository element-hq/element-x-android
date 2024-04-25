/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.migration.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_migration")
private val applicationMigrationVersion = intPreferencesKey("applicationMigrationVersion")

@ContributesBinding(AppScope::class)
class DefaultMigrationStore @Inject constructor(
    @ApplicationContext context: Context,
) : MigrationStore {
    private val store = context.dataStore

    override suspend fun setApplicationMigrationVersion(version: Int) {
        store.edit { prefs ->
            prefs[applicationMigrationVersion] = version
        }
    }

    override fun applicationMigrationVersion(): Flow<Int> {
        return store.data.map { prefs ->
            prefs[applicationMigrationVersion] ?: 0
        }
    }
}
