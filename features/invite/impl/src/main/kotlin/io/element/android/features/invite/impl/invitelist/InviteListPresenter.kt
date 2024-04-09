/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.invite.impl.invitelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInvitePresenter
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.invite.impl.model.InviteListInviteSummary
import io.element.android.features.invite.impl.model.InviteSender
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InviteListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val store: SeenInvitesStore,
    private val acceptDeclineInvitePresenter: AcceptDeclineInvitePresenter,
) : Presenter<InviteListState> {
    @Composable
    override fun present(): InviteListState {
        val invites by client
            .roomListService
            .invites
            .summaries
            .collectAsState(initial = emptyList())

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

        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        fun handleEvent(event: InviteListEvents) {
            when (event) {
                is InviteListEvents.AcceptInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(event.invite.toInviteData())
                    )
                }

                is InviteListEvents.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(event.invite.toInviteData())
                    )
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
            acceptDeclineInviteState = acceptDeclineInviteState,
            eventSink = ::handleEvent
        )
    }

    private fun RoomSummary.Filled.toInviteSummary(seen: Boolean) = details.run {
        val i = inviter
        val avatarData = if (isDirect && i != null) {
            AvatarData(
                id = i.userId.value,
                name = i.displayName,
                url = i.avatarUrl,
                size = AvatarSize.RoomInviteItem,
            )
        } else {
            AvatarData(
                id = roomId.value,
                name = name,
                url = avatarUrl,
                size = AvatarSize.RoomInviteItem,
            )
        }

        val alias = if (isDirect) {
            inviter?.userId?.value
        } else {
            canonicalAlias
        }

        InviteListInviteSummary(
            roomId = roomId,
            roomName = name,
            roomAlias = alias,
            roomAvatarData = avatarData,
            isDirect = isDirect,
            isNew = !seen,
            sender = inviter
                ?.takeIf { !isDirect }
                ?.run {
                    InviteSender(
                        userId = userId,
                        displayName = displayName ?: "",
                        avatarData = AvatarData(
                            id = userId.value,
                            name = displayName,
                            url = avatarUrl,
                            size = AvatarSize.InviteSender,
                        ),
                    )
                },
        )
    }

    private fun InviteListInviteSummary.toInviteData() = InviteData(
        roomId = roomId,
        roomName = roomName,
        isDirect = isDirect,
    )
}
