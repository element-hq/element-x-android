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

package io.element.android.libraries.designsystem.components

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bloom_store")

class BloomBlurHashStore(
    context: Context,
) {
    private val store = context.dataStore

    fun get(id: String): Flow<String?> {
        return store.data.map { prefs ->
            prefs[getBlurHashPreferenceKey(id)]
        }
    }

    suspend fun put(id: String, blurHash: String) {
        store.edit { prefs ->
            prefs[getBlurHashPreferenceKey(id)] = blurHash
        }
    }

    private fun getBlurHashPreferenceKey(id: String) = stringPreferencesKey("blur_hash_$id")
}



