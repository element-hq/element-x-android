/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.permissions.api.PermissionsStore
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class DefaultPermissionsStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : PermissionsStore {
    private val store = preferenceDataStoreFactory.create("permissions_store")

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
