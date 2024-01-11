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

package io.element.android.features.invitelist.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invitelist.api.SeenInvitesStore
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_seeninvites")
private val seenInvitesKey = stringSetPreferencesKey("seenInvites")

@ContributesBinding(SessionScope::class)
class DefaultSeenInvitesStore @Inject constructor(
    @ApplicationContext context: Context
) : SeenInvitesStore {
    private val store = context.dataStore

    override fun seenRoomIds(): Flow<Set<RoomId>> =
        store.data.map { prefs ->
            prefs[seenInvitesKey]
                .orEmpty()
                .map { RoomId(it) }
                .toSet()
        }

    override suspend fun markAsSeen(roomIds: Set<RoomId>) {
        store.edit { prefs ->
            prefs[seenInvitesKey] = roomIds.map { it.value }.toSet()
        }
    }
}
