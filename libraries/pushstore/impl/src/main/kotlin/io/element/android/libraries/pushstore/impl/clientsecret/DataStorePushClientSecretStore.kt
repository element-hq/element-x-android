/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DataStorePushClientSecretStore @Inject constructor(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : PushClientSecretStore {
    private val dataStore = preferenceDataStoreFactory.create("push_client_secret_store")

    override suspend fun storeSecret(userId: SessionId, clientSecret: String) {
        dataStore.edit { settings ->
            settings[getPreferenceKeyForUser(userId)] = clientSecret
        }
    }

    override suspend fun getSecret(userId: SessionId): String? {
        return dataStore.data.first()[getPreferenceKeyForUser(userId)]
    }

    override suspend fun resetSecret(userId: SessionId) {
        dataStore.edit { settings ->
            settings.remove(getPreferenceKeyForUser(userId))
        }
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        val keyValues = dataStore.data.first().asMap()
        val matchingKey = keyValues.keys.find {
            keyValues[it] == clientSecret
        }
        return matchingKey?.name?.let(::SessionId)
    }

    private fun getPreferenceKeyForUser(userId: SessionId) = stringPreferencesKey(userId.value)
}
