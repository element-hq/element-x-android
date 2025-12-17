/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val seenInvitesKey = stringSetPreferencesKey("seenInvites")

class DefaultSeenInvitesStore(
    context: Context,
    sessionId: SessionId,
    sessionCoroutineScope: CoroutineScope,
    sessionObserver: SessionObserver,
) : SeenInvitesStore {
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
        context.preferencesDataStoreFile("session_${hashedUserId}_seen-invites")
    }

    private val store = PreferenceDataStoreFactory.create(
        scope = sessionCoroutineScope,
        migrations = emptyList(),
    ) {
        dataStoreFile
    }

    override fun seenRoomIds(): Flow<Set<RoomId>> =
        store.data.map { prefs ->
            prefs[seenInvitesKey]
                .orEmpty()
                .map { RoomId(it) }
                .toSet()
        }

    override suspend fun markAsSeen(roomId: RoomId) {
        store.edit { prefs ->
            prefs[seenInvitesKey] = prefs[seenInvitesKey].orEmpty() + roomId.value
        }
    }

    override suspend fun markAsUnSeen(roomId: RoomId) {
        store.edit { prefs ->
            prefs[seenInvitesKey] = prefs[seenInvitesKey].orEmpty() - roomId.value
        }
    }

    override suspend fun clear() {
        dataStoreFile.safeDelete()
    }
}
