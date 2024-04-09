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

package io.element.android.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationEvents
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.powerlevels.canInvite
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomMemberListPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    private val roomMemberListDataSource: RoomMemberListDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMembersModerationPresenter: RoomMembersModerationPresenter,
    @Assisted private val navigator: RoomMemberListNavigator,
) : Presenter<RoomMemberListState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: RoomMemberListNavigator): RoomMemberListPresenter
    }

    @Composable
    override fun present(): RoomMemberListState {
        val coroutineScope = rememberCoroutineScope()
        var roomMembers by remember { mutableStateOf(RoomMembers.loading()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchResults by remember {
            mutableStateOf<SearchBarResultState<RoomMembers>>(SearchBarResultState.Initial())
        }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }

        val membersState by room.membersStateFlow.collectAsState()
        val canInvite by produceState(initialValue = false, key1 = membersState) {
            value = room.canInvite().getOrElse { false }
        }

        val roomModerationState = roomMembersModerationPresenter.present()

        // Ensure we load the latest data when entering this screen
        LaunchedEffect(Unit) {
            room.updateMembers()
        }

        LaunchedEffect(membersState) {
            if (membersState is MatrixRoomMembersState.Unknown) {
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
                roomMembers = RoomMembers(
                    invited = members.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                    joined = members.getOrDefault(RoomMembershipState.JOIN, emptyList())
                        .sortedWith(PowerLevelRoomMemberComparator())
                        .toImmutableList(),
                    banned = members.getOrDefault(RoomMembershipState.BAN, emptyList()).sortedBy { it.userId.value }.toImmutableList(),
                    isLoading = membersState is MatrixRoomMembersState.Pending,
                )
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
                        SearchBarResultState.Results(
                            RoomMembers(
                                invited = results.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                                joined = results.getOrDefault(RoomMembershipState.JOIN, emptyList())
                                    .sortedWith(PowerLevelRoomMemberComparator())
                                    .toImmutableList(),
                                banned = results.getOrDefault(RoomMembershipState.BAN, emptyList()).sortedBy { it.userId.value }.toImmutableList(),
                                isLoading = membersState is MatrixRoomMembersState.Pending,
                            )
                        )
                    }
                }
            }
        }

        fun handleEvents(event: RoomMemberListEvents) {
            when (event) {
                is RoomMemberListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                is RoomMemberListEvents.UpdateSearchQuery -> searchQuery = event.query
                is RoomMemberListEvents.RoomMemberSelected -> coroutineScope.launch {
                    if (roomMembersModerationPresenter.canDisplayModerationActions()) {
                        roomModerationState.eventSink(RoomMembersModerationEvents.SelectRoomMember(event.roomMember))
                    } else {
                        navigator.openRoomMemberDetails(event.roomMember.userId)
                    }
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
