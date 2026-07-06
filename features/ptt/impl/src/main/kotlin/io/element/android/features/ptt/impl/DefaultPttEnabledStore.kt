/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val pttEnabledRoomsKey = stringSetPreferencesKey("pttEnabledRooms")

/**
 * Session-scoped [PttEnabledStore] backed by a per-session preferences DataStore (mirrors
 * DefaultSeenInvitesStore). Must be a session singleton so there is a single DataStore instance per
 * file. Cleared when the session is deleted.
 */
@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
@Inject
class DefaultPttEnabledStore(
    @ApplicationContext context: Context,
    matrixClient: MatrixClient,
    sessionObserver: SessionObserver,
) : PttEnabledStore {
    private val sessionId = matrixClient.sessionId

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
        context.preferencesDataStoreFile("session_${hashedUserId}_ptt-enabled")
    }

    private val store = PreferenceDataStoreFactory.create(
        scope = matrixClient.sessionCoroutineScope,
        migrations = emptyList(),
    ) {
        dataStoreFile
    }

    override fun enabledRoomIds(): Flow<Set<RoomId>> =
        store.data.map { prefs ->
            prefs[pttEnabledRoomsKey].orEmpty().map { RoomId(it) }.toSet()
        }

    override suspend fun setEnabled(roomId: RoomId, enabled: Boolean) {
        store.edit { prefs ->
            prefs[pttEnabledRoomsKey] = if (enabled) {
                prefs[pttEnabledRoomsKey].orEmpty() + roomId.value
            } else {
                prefs[pttEnabledRoomsKey].orEmpty() - roomId.value
            }
        }
    }

    private suspend fun clear() {
        dataStoreFile.safeDelete()
    }
}
