/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents.ShowActionsForUser
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.map
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.ui.room.PowerLevelRoomMemberComparator
import io.element.android.libraries.matrix.ui.room.canInviteAsState
import io.element.android.libraries.matrix.ui.room.roomMemberIdentityStateChange
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@Inject
class RoomMemberListPresenter(
    private val room: JoinedRoom,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMembersModerationPresenter: Presenter<RoomMemberModerationState>,
    private val encryptionService: EncryptionService,
) : Presenter<RoomMemberListState> {
    private val powerLevelRoomMemberComparator = PowerLevelRoomMemberComparator()

    @Composable
    override fun present(): RoomMemberListState {
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val membersState by room.membersStateFlow.collectAsState()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canInvite by room.canInviteAsState(syncUpdateFlow.value)
        val roomModerationState = roomMembersModerationPresenter.present()

        val roomMemberIdentityStates by produceState(persistentMapOf()) {
            room.roomMemberIdentityStateChange(waitForEncryption = true)
                .onEach { identities ->
                    value = identities.associateBy({ it.identityRoomMember.userId }, { it.identityState }).toImmutableMap()
                }
                .launchIn(this)
        }

        var selectedSection by remember { mutableStateOf(SelectedSection.MEMBERS) }
        var roomMembers: AsyncData<RoomMembers> by remember { mutableStateOf(AsyncData.Loading()) }
        var filteredRoomMembers: AsyncData<RoomMembers> by remember { mutableStateOf(AsyncData.Loading()) }

        // Update the room members when the screen is loaded
        LaunchedEffect(Unit) {
            room.updateMembers()
        }

        LaunchedEffect(membersState, roomMemberIdentityStates) {
            if (membersState is RoomMembersState.Unknown) {
                return@LaunchedEffect
            }
            val finalMembersState = membersState
            if (finalMembersState is RoomMembersState.Error && finalMembersState.roomMembers().orEmpty().isEmpty()) {
                // Cannot fetch members and no cached members, display the error
                roomMembers = AsyncData.Failure(finalMembersState.failure)
                return@LaunchedEffect
            }
            withContext(coroutineDispatchers.io) {
                val members = membersState.roomMembers().orEmpty().groupBy { it.membership }
                val info = room.info()
                if (members.getOrDefault(RoomMembershipState.JOIN, emptyList()).size < info.joinedMembersCount / 2) {
                    // Don't display initial room member list if we have less than half of the joined members:
                    // This result will come from the timeline loading membership events and it'll be wrong.
                    return@withContext
                }
                val result = RoomMembers(
                    invited = members.getOrDefault(RoomMembershipState.INVITE, emptyList())
                        .map { it.withIdentityState(roomMemberIdentityStates) }
                        .toImmutableList(),
                    joined = members.getOrDefault(RoomMembershipState.JOIN, emptyList())
                        .sortedWith(powerLevelRoomMemberComparator)
                        .map { it.withIdentityState(roomMemberIdentityStates) }
                        .toImmutableList(),
                    banned = members.getOrDefault(RoomMembershipState.BAN, emptyList())
                        .sortedBy { it.userId.value }
                        .map { it.withIdentityState(roomMemberIdentityStates) }
                        .toImmutableList(),
                )
                roomMembers = if (membersState is RoomMembersState.Pending) {
                    AsyncData.Loading(result)
                } else {
                    AsyncData.Success(result)
                }
            }
        }

        LaunchedEffect(searchQuery, roomMembers) {
            filteredRoomMembers = roomMembers.map { members ->
                withContext(coroutineDispatchers.io) {
                    members.filter(searchQuery)
                }
            }
        }

        fun handleEvent(event: RoomMemberListEvents) {
            when (event) {
                is RoomMemberListEvents.UpdateSearchQuery -> searchQuery = event.query
                is RoomMemberListEvents.RoomMemberSelected ->
                    roomModerationState.eventSink(ShowActionsForUser(event.roomMember.toMatrixUser()))
                is RoomMemberListEvents.ChangeSelectedSection -> selectedSection = event.section
            }
        }

        val state = RoomMemberListState(
            roomMembers = roomMembers,
            filteredRoomMembers = filteredRoomMembers,
            searchQuery = searchQuery,
            canInvite = canInvite,
            moderationState = roomModerationState,
            selectedSection = selectedSection,
            eventSink = ::handleEvent,
        )
        if (!state.showBannedSection && selectedSection == SelectedSection.BANNED) {
            SideEffect {
                selectedSection = SelectedSection.MEMBERS
            }
        }
        return state
    }

    private suspend fun RoomMember.withIdentityState(identityStates: ImmutableMap<UserId, IdentityState>): RoomMemberWithIdentityState {
        return if (room.info().isEncrypted != true) {
            RoomMemberWithIdentityState(this, null)
        } else {
            val identityState = identityStates[userId] ?: encryptionService.getUserIdentity(userId, fallbackToServer = false).getOrNull()
            RoomMemberWithIdentityState(this, identityState)
        }
    }
}
