/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Local storage for:
 * - user consent (Boolean);
 * - did ask user consent (Boolean);
 * - analytics Id (String).
 */
interface AnalyticsStore {
    val userConsentFlow: Flow<Boolean>
    val didAskUserConsentFlow: Flow<Boolean>
    val analyticsIdFlow: Flow<String>
    suspend fun setUserConsent(newUserConsent: Boolean)
    suspend fun setDidAskUserConsent(newValue: Boolean = true)
    suspend fun setAnalyticsId(newAnalyticsId: String)
    suspend fun reset()
}

@ContributesBinding(AppScope::class)
class DefaultAnalyticsStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : AnalyticsStore {
    private val userConsent = booleanPreferencesKey("user_consent")
    private val didAskUserConsent = booleanPreferencesKey("did_ask_user_consent")
    private val analyticsId = stringPreferencesKey("analytics_id")

    private val dataStore = preferenceDataStoreFactory.create("vector_analytics")

    override val userConsentFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[userConsent].orFalse() }
        .distinctUntilChanged()

    override val didAskUserConsentFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[didAskUserConsent].orFalse() }
        .distinctUntilChanged()

    override val analyticsIdFlow: Flow<String> = dataStore.data
        .map { preferences -> preferences[analyticsId].orEmpty() }
        .distinctUntilChanged()

    override suspend fun setUserConsent(newUserConsent: Boolean) {
        dataStore.edit { settings ->
            settings[userConsent] = newUserConsent
        }
    }

    override suspend fun setDidAskUserConsent(newValue: Boolean) {
        dataStore.edit { settings ->
            settings[didAskUserConsent] = newValue
        }
    }

    override suspend fun setAnalyticsId(newAnalyticsId: String) {
        dataStore.edit { settings ->
            settings[analyticsId] = newAnalyticsId
        }
    }

    override suspend fun reset() {
        dataStore.edit {
            it.clear()
        }
    }
}
