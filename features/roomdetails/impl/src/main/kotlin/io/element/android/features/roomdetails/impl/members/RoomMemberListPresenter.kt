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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.powerlevels.canInvite
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomMemberListPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val roomMemberListDataSource: RoomMemberListDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<RoomMemberListState> {
    @Composable
    override fun present(): RoomMemberListState {
        var roomMembers by remember { mutableStateOf<AsyncData<RoomMembers>>(AsyncData.Loading()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchResults by remember {
            mutableStateOf<SearchBarResultState<RoomMembers>>(SearchBarResultState.Initial())
        }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }

        val membersState by room.membersStateFlow.collectAsState()
        val canInvite by produceState(initialValue = false, key1 = membersState) {
            value = room.canInvite().getOrElse { false }
        }

        LaunchedEffect(Unit) {
            withContext(coroutineDispatchers.io) {
                val members = roomMemberListDataSource.search("").groupBy { it.membership }
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = members.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                        joined = members.getOrDefault(RoomMembershipState.JOIN, emptyList()).toImmutableList(),
                    )
                )
            }
        }

        LaunchedEffect(searchQuery) {
            withContext(coroutineDispatchers.io) {
                searchResults = if (searchQuery.isEmpty()) {
                    SearchBarResultState.Initial()
                } else {
                    val results = roomMemberListDataSource.search(searchQuery).groupBy { it.membership }
                    if (results.isEmpty()) {
                        SearchBarResultState.NoResultsFound()
                    } else {
                        SearchBarResultState.Results(
                            RoomMembers(
                                invited = results.getOrDefault(RoomMembershipState.INVITE, emptyList()).toImmutableList(),
                                joined = results.getOrDefault(RoomMembershipState.JOIN, emptyList()).toImmutableList(),
                            )
                        )
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
            eventSink = { event ->
                when (event) {
                    is RoomMemberListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                    is RoomMemberListEvents.UpdateSearchQuery -> searchQuery = event.query
                }
            },
        )
    }
}
