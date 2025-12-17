/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Note: this will be used only in the nightly and in the debug build.
 */
@Inject
class PreferencesFeatureFlagProvider(
    private val buildMeta: BuildMeta,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : MutableFeatureFlagProvider {
    private val store = preferenceDataStoreFactory.create("elementx_featureflag")

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
