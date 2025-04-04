/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val seenInvitesKey = stringSetPreferencesKey("seenInvites")

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
class DefaultSeenInvitesStore @Inject constructor(
    @ApplicationContext context: Context,
    currentSessionIdHolder: CurrentSessionIdHolder,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    sessionObserver: SessionObserver,
) : SeenInvitesStore {
    private val sessionId: SessionId = currentSessionIdHolder.current

    init {
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionCreated(userId: String) = Unit
            override suspend fun onSessionDeleted(userId: String) {
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
