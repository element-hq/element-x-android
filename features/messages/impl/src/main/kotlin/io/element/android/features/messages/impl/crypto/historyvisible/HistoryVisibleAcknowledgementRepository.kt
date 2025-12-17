/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HistoryVisibleAcknowledgementRepository {
    fun hasAcknowledged(roomId: RoomId): Flow<Boolean>
    suspend fun setAcknowledged(roomId: RoomId, value: Boolean)
}

@ContributesBinding(SessionScope::class)
class DefaultHistoryVisibleAcknowledgementRepository(
    sessionId: SessionId,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : HistoryVisibleAcknowledgementRepository {
    val store =
        sessionId.value.hash().take(16).let { hash ->
            preferenceDataStoreFactory.create("elementx_historyvisible_$hash")
        }

    override fun hasAcknowledged(roomId: RoomId): Flow<Boolean> {
        return store.data.map { prefs ->
            val acknowledged = prefs[booleanPreferencesKey(roomId.value)] ?: false
            acknowledged
        }
    }

    override suspend fun setAcknowledged(roomId: RoomId, value: Boolean) {
        store.edit { prefs ->
            prefs[booleanPreferencesKey(roomId.value)] = value
        }
    }
}
