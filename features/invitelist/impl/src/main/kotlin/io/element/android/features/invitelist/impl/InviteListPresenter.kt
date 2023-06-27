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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.invitelist.api.SeenInvitesStore
import io.element.android.features.invitelist.impl.model.InviteListInviteSummary
import io.element.android.features.invitelist.impl.model.InviteSender
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomSummary
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class InviteListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val store: SeenInvitesStore,
) : Presenter<InviteListState> {

    @Composable
    override fun present(): InviteListState {
        val invites by client
            .invitesDataSource
            .roomSummaries()
            .collectAsState()

        var seenInvites by remember { mutableStateOf<Set<RoomId>>(emptySet()) }

        LaunchedEffect(Unit) {
            seenInvites = store.seenRoomIds().first()
        }

        LaunchedEffect(invites) {
            store.markAsSeen(
                invites
                    .filterIsInstance<RoomSummary.Filled>()
                    .map { it.details.roomId }
                    .toSet()
            )
        }

        val localCoroutineScope = rememberCoroutineScope()
        val acceptedAction: MutableState<Async<RoomId>> = remember { mutableStateOf(Async.Uninitialized) }
        val declinedAction: MutableState<Async<Unit>> = remember { mutableStateOf(Async.Uninitialized) }
        val decliningInvite: MutableState<InviteListInviteSummary?> = remember { mutableStateOf(null) }

        fun handleEvent(event: InviteListEvents) {
            when (event) {
                is InviteListEvents.AcceptInvite -> {
                    acceptedAction.value = Async.Uninitialized
                    localCoroutineScope.acceptInvite(event.invite.roomId, acceptedAction)
                }

                is InviteListEvents.DeclineInvite -> {
                    decliningInvite.value = event.invite
                }

                is InviteListEvents.ConfirmDeclineInvite -> {
                    declinedAction.value = Async.Uninitialized
                    decliningInvite.value?.let {
                        localCoroutineScope.declineInvite(it.roomId, declinedAction)
                    }
                    decliningInvite.value = null
                }

                is InviteListEvents.CancelDeclineInvite -> {
                    decliningInvite.value = null
                }

                is InviteListEvents.DismissAcceptError -> {
                    acceptedAction.value = Async.Uninitialized
                }

                is InviteListEvents.DismissDeclineError -> {
                    declinedAction.value = Async.Uninitialized
                }
            }
        }

        val inviteList = remember(seenInvites, invites) {
            invites
                .filterIsInstance<RoomSummary.Filled>()
                .map {
                    it.toInviteSummary(seenInvites.contains(it.details.roomId))
                }
                .toPersistentList()
        }

        return InviteListState(
            inviteList = inviteList,
            declineConfirmationDialog = decliningInvite.value?.let {
                InviteDeclineConfirmationDialog.Visible(
                    isDirect = it.isDirect,
                    name = it.roomName,
                )
            } ?: InviteDeclineConfirmationDialog.Hidden,
            acceptedAction = acceptedAction.value,
            declinedAction = declinedAction.value,
            eventSink = ::handleEvent
        )
    }

    private fun CoroutineScope.acceptInvite(roomId: RoomId, acceptedAction: MutableState<Async<RoomId>>) = launch {
        suspend {
            client.getRoom(roomId)?.use {
                it.acceptInvitation().getOrThrow()
            }
            roomId
        }.runCatchingUpdatingState(acceptedAction)
    }

    private fun CoroutineScope.declineInvite(roomId: RoomId, declinedAction: MutableState<Async<Unit>>) = launch {
        suspend {
            client.getRoom(roomId)?.use {
                it.rejectInvitation().getOrThrow()
            } ?: Unit
        }.runCatchingUpdatingState(declinedAction)
    }

    private fun RoomSummary.Filled.toInviteSummary(seen: Boolean) = details.run {
        val i = inviter
        val avatarData = if (isDirect && i != null)
            AvatarData(
                id = i.userId.value,
                name = i.displayName,
                url = i.avatarUrl,
            )
        else
            AvatarData(
                id = roomId.value,
                name = name,
                url = avatarURLString
            )

        val alias = if (isDirect)
            inviter?.userId?.value
        else
            canonicalAlias

        InviteListInviteSummary(
            roomId = roomId,
            roomName = name,
            roomAlias = alias,
            roomAvatarData = avatarData,
            isDirect = isDirect,
            isNew = !seen,
            sender = if (isDirect) null else inviter?.run {
                InviteSender(
                    userId = userId,
                    displayName = displayName ?: "",
                    avatarData = AvatarData(
                        id = userId.value,
                        name = displayName,
                        url = avatarUrl,
                    ),
                )
            },
        )
    }
}
