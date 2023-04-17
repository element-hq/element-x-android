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

package io.element.android.libraries.featureflag.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.featureflag.api.Feature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_featureflag")

class PreferencesFeatureFlagProvider @Inject constructor(@ApplicationContext context: Context) : RuntimeFeatureFlagProvider {

    private val store = context.dataStore

    override val priority: Int
        get() = MEDIUM_PRIORITY

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean) {
        store.edit { prefs ->
            prefs[booleanPreferencesKey(feature.key)] = enabled
        }
    }

    override suspend fun isFeatureEnabled(feature: Feature): Boolean {
        return store.data.map { prefs ->
            prefs[booleanPreferencesKey(feature.key)] ?: feature.defaultValue
        }.first()
    }

    override fun hasFeature(feature: Feature): Boolean {
        return true
    }
}
