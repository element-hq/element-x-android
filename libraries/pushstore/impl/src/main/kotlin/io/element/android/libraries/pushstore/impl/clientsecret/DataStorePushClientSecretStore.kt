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

package io.element.android.libraries.pushstore.impl.clientsecret

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecretStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "push_client_secret_store")

@ContributesBinding(AppScope::class)
class DataStorePushClientSecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : PushClientSecretStore {
    override suspend fun storeSecret(userId: SessionId, clientSecret: String) {
        context.dataStore.edit { settings ->
            settings[getPreferenceKeyForUser(userId)] = clientSecret
        }
    }

    override suspend fun getSecret(userId: SessionId): String? {
        return context.dataStore.data.first()[getPreferenceKeyForUser(userId)]
    }

    override suspend fun resetSecret(userId: SessionId) {
        context.dataStore.edit { settings ->
            settings.remove(getPreferenceKeyForUser(userId))
        }
    }

    override suspend fun getUserIdFromSecret(clientSecret: String): SessionId? {
        val keyValues = context.dataStore.data.first().asMap()
        val matchingKey = keyValues.keys.find {
            keyValues[it] == clientSecret
        }
        return matchingKey?.name?.let(::SessionId)
    }

    private fun getPreferenceKeyForUser(userId: SessionId) = stringPreferencesKey(userId.value)
}
