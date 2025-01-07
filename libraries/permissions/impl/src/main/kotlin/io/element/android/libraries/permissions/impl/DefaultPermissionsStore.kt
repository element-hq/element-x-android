/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.permissions.api.PermissionsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "permissions_store")

@ContributesBinding(AppScope::class)
class DefaultPermissionsStore @Inject constructor(
    @ApplicationContext private val context: Context,
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
