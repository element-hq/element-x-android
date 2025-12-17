/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.preferences.DefaultPreferencesCorruptionHandlerFactory
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPreferencesDataStoreFactory(
    @ApplicationContext private val context: Context,
) : PreferenceDataStoreFactory {
    private val dataStoreHolders = ConcurrentHashMap<String, DataStoreHolder>()

    private class DataStoreHolder(name: String) {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = name,
            corruptionHandler = DefaultPreferencesCorruptionHandlerFactory.replaceWithEmpty(),
        )
    }

    override fun create(name: String): DataStore<Preferences> {
        val holder = dataStoreHolders.getOrPut(name) {
            DataStoreHolder(name)
        }
        return with(holder) {
            context.dataStore
        }
    }
}
