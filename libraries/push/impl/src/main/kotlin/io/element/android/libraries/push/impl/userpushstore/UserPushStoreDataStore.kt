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

package io.element.android.libraries.push.impl.userpushstore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/**
 * Store data related to push about a user.
 */
class UserPushStoreDataStore(
    private val context: Context,
    userId: String,
) : UserPushStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "push_store_$userId")
    private val notificationMethod = stringPreferencesKey("notificationMethod")
    private val currentPushKey = stringPreferencesKey("currentPushKey")

    override suspend fun getNotificationMethod(): String {
        return context.dataStore.data.first()[notificationMethod] ?: NOTIFICATION_METHOD_FIREBASE
    }

    override suspend fun setNotificationMethod(value: String) {
        context.dataStore.edit {
            it[notificationMethod] = value
        }
    }

    override suspend fun getCurrentRegisteredPushKey(): String? {
        return context.dataStore.data.first()[currentPushKey]
    }

    override suspend fun setCurrentRegisteredPushKey(value: String) {
        context.dataStore.edit {
            it[currentPushKey] = value
        }
    }

    override suspend fun reset() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
