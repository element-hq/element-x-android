/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
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
    factory: PreferenceDataStoreFactory,
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

    private val store: DataStore<Preferences> = factory.create(preferenceName)
    private val pushProviderName = stringPreferencesKey("pushProviderName")
    private val currentPushKey = stringPreferencesKey("currentPushKey")
    private val notificationEnabled = booleanPreferencesKey("notificationEnabled")
    private val ignoreRegistrationError = booleanPreferencesKey("ignoreRegistrationError")

    override suspend fun getPushProviderName(): String? {
        return store.data.first()[pushProviderName]
    }

    override suspend fun setPushProviderName(value: String) {
        store.edit {
            it[pushProviderName] = value
        }
    }

    override suspend fun getCurrentRegisteredPushKey(): String? {
        return store.data.first()[currentPushKey]
    }

    override suspend fun setCurrentRegisteredPushKey(value: String?) {
        store.edit {
            if (value == null) {
                it.remove(currentPushKey)
            } else {
                it[currentPushKey] = value
            }
        }
    }

    override fun getNotificationEnabledForDevice(): Flow<Boolean> {
        return store.data.map { it[notificationEnabled].orTrue() }
    }

    override suspend fun setNotificationEnabledForDevice(enabled: Boolean) {
        store.edit {
            it[notificationEnabled] = enabled
        }
    }

    override fun useCompleteNotificationFormat(): Boolean {
        return true
    }

    override fun ignoreRegistrationError(): Flow<Boolean> {
        return store.data.map { it[ignoreRegistrationError].orFalse() }
    }

    override suspend fun setIgnoreRegistrationError(ignore: Boolean) {
        store.edit {
            it[ignoreRegistrationError] = ignore
        }
    }

    override suspend fun reset() {
        store.edit {
            it.clear()
        }
        // Also delete the file
        context.preferencesDataStoreFile(preferenceName).delete()
    }
}
