/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.store

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.store.CustomNotificationChannelsStore
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val customChannelsKey = stringSetPreferencesKey("customNotificationChannels")

class DefaultCustomNotificationChannelsStore(
    context: Context,
    sessionId: SessionId,
    sessionCoroutineScope: CoroutineScope,
    sessionObserver: SessionObserver,
) : CustomNotificationChannelsStore {
    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                if (sessionId.value == userId) {
                    clear()
                }
            }
        })
    }

    private val dataStoreFile = sessionId.value.hash().take(16).let { hashedUserId ->
        context.preferencesDataStoreFile("session_${hashedUserId}_custom-notification-channels")
    }

    private val store = PreferenceDataStoreFactory.create(
        scope = sessionCoroutineScope,
        migrations = emptyList(),
    ) {
        dataStoreFile
    }

    override fun roomIdsWithCustomChannel(): Flow<Set<RoomId>> =
        store.data.map { prefs ->
            prefs[customChannelsKey]
                .orEmpty()
                .map { RoomId(it) }
                .toSet()
        }

    override suspend fun hasCustomChannel(roomId: RoomId): Boolean {
        return roomIdsWithCustomChannel().first().contains(roomId)
    }

    override suspend fun addCustomChannel(roomId: RoomId) {
        store.edit { prefs ->
            prefs[customChannelsKey] = prefs[customChannelsKey].orEmpty() + roomId.value
        }
    }

    override suspend fun removeCustomChannel(roomId: RoomId) {
        store.edit { prefs ->
            prefs[customChannelsKey] = prefs[customChannelsKey].orEmpty() - roomId.value
        }
    }

    override suspend fun clear() {
        store.edit { prefs ->
            prefs.remove(customChannelsKey)
        }
        dataStoreFile.safeDelete()
    }
}
