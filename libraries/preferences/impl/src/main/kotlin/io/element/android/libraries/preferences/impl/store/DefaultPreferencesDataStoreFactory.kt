/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultPreferencesDataStoreFactory(
    @ApplicationContext private val context: Context,
) : PreferenceDataStoreFactory {
    private class DataStoreHolder(name: String) {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = name)
    }
    override fun create(name: String): DataStore<Preferences> {
        return with(DataStoreHolder(name)) {
            context.dataStore
        }
    }
}
