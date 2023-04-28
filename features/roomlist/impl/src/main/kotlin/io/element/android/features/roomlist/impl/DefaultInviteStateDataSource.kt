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

package io.element.android.features.roomlist.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invitelist.api.SeenInvitesStore
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultInviteStateDataSource @Inject constructor(
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : InviteStateDataSource {

    override fun inviteState(): Flow<InvitesState> =
        client.invitesDataSource
            .roomSummaries()
            .combine(seenInvitesStore.seenRoomIds()) { invites, seenIds ->
                when {
                    invites.isEmpty() -> InvitesState.NoInvites
                    seenIds.containsAll(invites.roomIds) -> InvitesState.SeenInvites
                    else -> InvitesState.NewInvites
                }
            }
}

private val List<RoomSummary>.roomIds: Collection<RoomId>
    get() = filterIsInstance<RoomSummary.Filled>().map { it.details.roomId }
