/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_crash")

private val appHasCrashedKey = booleanPreferencesKey("appHasCrashed")
private val crashDataKey = stringPreferencesKey("crashData")

@ContributesBinding(AppScope::class)
class PreferencesCrashDataStore @Inject constructor(
    @ApplicationContext context: Context
) : CrashDataStore {
    private val store = context.dataStore

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
