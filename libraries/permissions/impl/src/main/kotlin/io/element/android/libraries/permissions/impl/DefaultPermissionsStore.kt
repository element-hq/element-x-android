/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.permissions.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "permissions_store")

@ContributesBinding(AppScope::class)
class DefaultPermissionsStore @Inject constructor(
    @ApplicationContext context: Context,
) : PermissionsStore {
    private val store = context.dataStore

    override suspend fun setPermissionDenied(permission: String, value: Boolean) {
        store.edit { prefs ->
            prefs[getDeniedPreferenceKey(permission)] = value
        }
    }

    override fun isPermissionDenied(permission: String): Flow<Boolean> {
        return store.data.map {
            it[getDeniedPreferenceKey(permission)].orFalse()
        }
    }

    override suspend fun setPermissionAsked(permission: String, value: Boolean) {
        store.edit { prefs ->
            prefs[getAskedPreferenceKey(permission)] = value
        }
    }

    override fun isPermissionAsked(permission: String): Flow<Boolean> {
        return store.data.map {
            it[getAskedPreferenceKey(permission)].orFalse()
        }
    }

    override suspend fun resetPermission(permission: String) {
        setPermissionAsked(permission, false)
        setPermissionDenied(permission, false)
    }

    override suspend fun resetStore() {
        store.edit { it.clear() }
    }

    private fun getDeniedPreferenceKey(permission: String) = booleanPreferencesKey("${permission}_denied")
    private fun getAskedPreferenceKey(permission: String) = booleanPreferencesKey("${permission}_asked")
}
