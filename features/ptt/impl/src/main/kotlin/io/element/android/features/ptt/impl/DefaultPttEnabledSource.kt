/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.ptt.api.PttEnabledSource
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

private val pttEnabledRoomsKey = stringSetPreferencesKey("pttEnabledRooms")

/**
 * App-scoped [PttEnabledSource] backed by per-session preferences DataStores (one file per session,
 * mirroring the previous session-scoped store). App-scoped so it can be read from the push pipeline;
 * owns the single DataStore instance per file (DataStore forbids more than one per file per process),
 * caching them by session id.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class DefaultPttEnabledSource(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    sessionObserver: SessionObserver,
) : PttEnabledSource {
    private val stores = ConcurrentHashMap<String, DataStore<Preferences>>()

    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                clear(SessionId(userId))
            }
        })
    }

    override fun enabledRoomIdsFlow(sessionId: SessionId): Flow<Set<RoomId>> =
        store(sessionId).data.map { prefs ->
            prefs[pttEnabledRoomsKey].orEmpty().map { RoomId(it) }.toSet()
        }

    override suspend fun isPttEnabled(sessionId: SessionId, roomId: RoomId): Boolean =
        enabledRoomIdsFlow(sessionId).first().contains(roomId)

    override suspend fun setEnabled(sessionId: SessionId, roomId: RoomId, enabled: Boolean) {
        store(sessionId).edit { prefs ->
            prefs[pttEnabledRoomsKey] = if (enabled) {
                prefs[pttEnabledRoomsKey].orEmpty() + roomId.value
            } else {
                prefs[pttEnabledRoomsKey].orEmpty() - roomId.value
            }
        }
    }

    private fun store(sessionId: SessionId): DataStore<Preferences> =
        stores.computeIfAbsent(sessionId.value) {
            PreferenceDataStoreFactory.create(scope = appCoroutineScope) {
                context.preferencesDataStoreFile(fileNameFor(sessionId))
            }
        }

    private fun clear(sessionId: SessionId) {
        stores.remove(sessionId.value)
        context.preferencesDataStoreFile(fileNameFor(sessionId)).safeDelete()
    }

    private fun fileNameFor(sessionId: SessionId): String =
        "session_${sessionId.value.hash().take(16)}_ptt-enabled"
}
