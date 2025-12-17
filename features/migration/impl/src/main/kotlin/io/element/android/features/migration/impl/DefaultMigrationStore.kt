/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val applicationMigrationVersion = intPreferencesKey("applicationMigrationVersion")

@ContributesBinding(AppScope::class)
class DefaultMigrationStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : MigrationStore {
    private val store = preferenceDataStoreFactory.create("elementx_migration")

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
