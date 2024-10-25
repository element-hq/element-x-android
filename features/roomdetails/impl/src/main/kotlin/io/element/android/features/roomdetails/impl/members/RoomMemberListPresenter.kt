/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationEvents
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.room.canInviteAsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RoomMemberListPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    private val roomMemberListDataSource: RoomMemberListDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMembersModerationPresenter: Presenter<RoomMembersModerationState>,
    @Assisted private val navigator: RoomMemberListNavigator,
) : Presenter<RoomMemberListState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: RoomMemberListNavigator): RoomMemberListPresenter
    }

    @Composable
    override fun present(): RoomMemberListState {
        var roomMembers: AsyncData<RoomMembers> by remember { mutableStateOf(AsyncData.Loading()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchResults by remember {
            mutableStateOf<SearchBarResultState<AsyncData<RoomMembers>>>(SearchBarResultState.Initial())
        }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }

        val membersState by room.membersStateFlow.collectAsState()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canInvite by room.canInviteAsState(syncUpdateFlow.value)

        val roomModerationState = roomMembersModerationPresenter.present()

        // Ensure we load the latest data when entering this screen
        LaunchedEffect(Unit) {
            room.updateMembers()
        }

        LaunchedEffect(membersState) {
            if (membersState is MatrixRoomMembersState.Unknown) {
                return@LaunchedEffect
            }
            val finalMembersState = membersState
            if (finalMembersState is MatrixRoomMembersState.Error && finalMembersState.roomMembers().orEmpty().isEmpty()) {
                // Cannot fetch members and no cached members, display the error
                roomMembers = AsyncData.Failure(finalMembersState.failure)
                return@LaunchedEffect
            }
            withContext(coroutineDispatchers.io) {
                val members = membersState.roomMembers().orEmpty().groupBy { it.membership }
                val info = room.roomInfoFlow.first()
                if (members.getOrDefault(RoomMembershipState.JOIN, emptyList()).size < info.joinedMembersCount / 2) {
                    // Don't display initial room member list if we have less than half of the joined members:
                    // This result will come from the timeline loading membership events and it'll be wrong.
                    return@withContext
                }
                val result = RoomMembers(
                    invited = members.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                    joined = members.getOrDefault(RoomMembershipState.JOIN, emptyList())
                        .sortedWith(PowerLevelRoomMemberComparator())
                        .toImmutableList(),
                    banned = members.getOrDefault(RoomMembershipState.BAN, emptyList()).sortedBy { it.userId.value }.toImmutableList(),
                )
                roomMembers = if (membersState is MatrixRoomMembersState.Pending) {
                    AsyncData.Loading(result)
                } else {
                    AsyncData.Success(result)
                }
            }
        }

        LaunchedEffect(membersState, searchQuery, isSearchActive) {
            withContext(coroutineDispatchers.io) {
                searchResults = if (searchQuery.isEmpty() || !isSearchActive) {
                    SearchBarResultState.Initial()
                } else {
                    val results = roomMemberListDataSource.search(searchQuery).groupBy { it.membership }
                    if (results.isEmpty()) {
                        SearchBarResultState.NoResultsFound()
                    } else {
                        val result = RoomMembers(
                            invited = results.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                            joined = results.getOrDefault(RoomMembershipState.JOIN, emptyList())
                                .sortedWith(PowerLevelRoomMemberComparator())
                                .toImmutableList(),
                            banned = results.getOrDefault(RoomMembershipState.BAN, emptyList()).sortedBy { it.userId.value }.toImmutableList(),
                        )
                        SearchBarResultState.Results(
                            if (membersState is MatrixRoomMembersState.Pending) {
                                AsyncData.Loading(result)
                            } else {
                                AsyncData.Success(result)
                            }
                        )
                    }
                }
            }
        }

        fun handleEvents(event: RoomMemberListEvents) {
            when (event) {
                is RoomMemberListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                is RoomMemberListEvents.UpdateSearchQuery -> searchQuery = event.query
                is RoomMemberListEvents.RoomMemberSelected ->
                    if (roomModerationState.canDisplayModerationActions) {
                        roomModerationState.eventSink(RoomMembersModerationEvents.SelectRoomMember(event.roomMember))
                    } else {
                        navigator.openRoomMemberDetails(event.roomMember.userId)
                    }
            }
        }

        return RoomMemberListState(
            roomMembers = roomMembers,
            searchQuery = searchQuery,
            searchResults = searchResults,
            isSearchActive = isSearchActive,
            canInvite = canInvite,
            moderationState = roomModerationState,
            eventSink = { handleEvents(it) },
        )
    }
}
