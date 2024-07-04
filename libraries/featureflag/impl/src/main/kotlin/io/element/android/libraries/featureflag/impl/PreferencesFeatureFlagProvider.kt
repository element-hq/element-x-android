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
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.featureflag.api.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_featureflag")

/**
 * Note: this will be used only in the nightly and in the debug build.
 */
class PreferencesFeatureFlagProvider @Inject constructor(
    @ApplicationContext context: Context,
    private val buildMeta: BuildMeta,
) : MutableFeatureFlagProvider {
    private val store = context.dataStore

    override val priority = MEDIUM_PRIORITY

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean) {
        store.edit { prefs ->
            prefs[booleanPreferencesKey(feature.key)] = enabled
        }
    }

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[booleanPreferencesKey(feature.key)] ?: feature.defaultValue(buildMeta)
        }.distinctUntilChanged()
    }

    override fun hasFeature(feature: Feature): Boolean {
        return true
    }
}
