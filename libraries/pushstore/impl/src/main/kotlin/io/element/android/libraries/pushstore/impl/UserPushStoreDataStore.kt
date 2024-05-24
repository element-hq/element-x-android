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

package io.element.android.libraries.pushstore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.UserPushStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Store data related to push about a user.
 */
class UserPushStoreDataStore(
    private val context: Context,
    userId: SessionId,
) : UserPushStore {
    // Hash the sessionId to get rid of exotic chars and take only the first 16 chars.
    // The risk of collision is not high.
    private val preferenceName = "push_store_${userId.value.hash().take(16)}"

    init {
        // Migrate legacy data. Previous file can be too long if the userId is too long. The userId can be up to 255 chars.
        // Example of long file path, with `averylonguserid` replacing a very longer name
        // /data/user/0/io.element.android.x.debug/files/datastore/push_store_@averylonguserid:example.org.preferences_pb
        val legacyFile = context.preferencesDataStoreFile("push_store_$userId")
        if (legacyFile.exists()) {
            Timber.d("Migrating legacy push data store for $userId")
            if (!legacyFile.renameTo(context.preferencesDataStoreFile(preferenceName))) {
                Timber.w("Failed to migrate legacy push data store for $userId")
            }
        }
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = preferenceName)
    private val pushProviderName = stringPreferencesKey("pushProviderName")
    private val currentPushKey = stringPreferencesKey("currentPushKey")
    private val notificationEnabled = booleanPreferencesKey("notificationEnabled")

    override suspend fun getPushProviderName(): String? {
        return context.dataStore.data.first()[pushProviderName]
    }

    override suspend fun setPushProviderName(value: String) {
        context.dataStore.edit {
            it[pushProviderName] = value
        }
    }

    override suspend fun getCurrentRegisteredPushKey(): String? {
        return context.dataStore.data.first()[currentPushKey]
    }

    override suspend fun setCurrentRegisteredPushKey(value: String?) {
        context.dataStore.edit {
            if (value == null) {
                it.remove(currentPushKey)
            } else {
                it[currentPushKey] = value
            }
        }
    }

    override fun getNotificationEnabledForDevice(): Flow<Boolean> {
        return context.dataStore.data.map { it[notificationEnabled].orTrue() }
    }

    override suspend fun setNotificationEnabledForDevice(enabled: Boolean) {
        context.dataStore.edit {
            it[notificationEnabled] = enabled
        }
    }

    override fun useCompleteNotificationFormat(): Boolean {
        return true
    }

    override suspend fun reset() {
        context.dataStore.edit {
            it.clear()
        }
        // Also delete the file
        context.preferencesDataStoreFile(preferenceName).delete()
    }
}
