/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Also accessed via reflection by the instrumentation tests @see [im.vector.app.ClearCurrentSessionRule].
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "map_type_store")

/**
 * Local storage for:
 * - map tile provider (String).
 */
interface MapStore {
    val mapTileProviderFlow: Flow<String>
    suspend fun setMapTileProvider(provider: String)
}

@ContributesBinding(AppScope::class)
class MapTypeStore @Inject constructor(
    @ApplicationContext private val context: Context
) : MapStore {
    private val mapTileProvider = stringPreferencesKey("map_tile_provider")

    override val mapTileProviderFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[mapTileProvider] ?: "streets-v2"
    }

    override suspend fun setMapTileProvider(provider: String) {
        context.dataStore.edit { settings ->
            settings[mapTileProvider] = provider
        }
    }
}
