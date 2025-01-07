/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
            prefs[applicationMigrationVersion] ?: -1
        }
    }
}
