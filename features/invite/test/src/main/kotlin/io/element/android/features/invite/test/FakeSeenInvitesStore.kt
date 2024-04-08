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

package io.element.android.features.invite.test

import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSeenInvitesStore : SeenInvitesStore {
    private val existing = MutableStateFlow(emptySet<RoomId>())
    private var provided: Set<RoomId>? = null

    fun publishRoomIds(invites: Set<RoomId>) {
        existing.value = invites
    }

    fun getProvidedRoomIds() = provided

    override fun seenRoomIds(): Flow<Set<RoomId>> = existing

    override suspend fun markAsSeen(roomIds: Set<RoomId>) {
        provided = roomIds.toSet()
    }
}
