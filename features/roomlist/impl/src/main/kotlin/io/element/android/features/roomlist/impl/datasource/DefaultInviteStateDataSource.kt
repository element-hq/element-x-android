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

package io.element.android.features.roomlist.impl.datasource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.roomlist.impl.InvitesState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultInviteStateDataSource @Inject constructor(
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
    private val coroutineDispatchers: CoroutineDispatchers,
) : InviteStateDataSource {
    @Composable
    override fun inviteState(): InvitesState {
        val invites by client
            .roomListService
            .invites
            .summaries
            .collectAsState(initial = emptyList())

        val seenInvites by seenInvitesStore
            .seenRoomIds()
            .collectAsState(initial = emptySet())

        var state by remember { mutableStateOf(InvitesState.NoInvites) }

        LaunchedEffect(invites, seenInvites) {
            withContext(coroutineDispatchers.computation) {
                state = when {
                    invites.isEmpty() -> InvitesState.NoInvites
                    seenInvites.containsAll(invites.roomIds) -> InvitesState.SeenInvites
                    else -> InvitesState.NewInvites
                }
            }
        }

        return state
    }
}

private val List<RoomSummary>.roomIds: Collection<RoomId>
    get() = filterIsInstance<RoomSummary.Filled>().map { it.details.roomId }
