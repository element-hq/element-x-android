/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val appHasCrashedKey = booleanPreferencesKey("appHasCrashed")
private val crashDataKey = stringPreferencesKey("crashData")

@ContributesBinding(AppScope::class)
class PreferencesCrashDataStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : CrashDataStore {
    private val store = preferenceDataStoreFactory.create("elementx_crash")

    override fun setCrashData(crashData: String) {
        // Must block
        runBlocking {
            store.edit { prefs ->
                prefs[appHasCrashedKey] = true
                prefs[crashDataKey] = crashData
            }
        }
    }

    override suspend fun resetAppHasCrashed() {
        store.edit { prefs ->
            prefs[appHasCrashedKey] = false
        }
    }

    override fun appHasCrashed(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[appHasCrashedKey].orFalse()
        }
    }

    override fun crashInfo(): Flow<String> {
        return store.data.map { prefs ->
            prefs[crashDataKey].orEmpty()
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}
