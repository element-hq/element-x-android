/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
