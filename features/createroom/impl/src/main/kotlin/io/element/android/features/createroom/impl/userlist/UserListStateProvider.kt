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

package io.element.android.features.createroom.impl.userlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.recent.RecentDirectRoom
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

open class UserListStateProvider : PreviewParameterProvider<UserListState> {
    override val values: Sequence<UserListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState(
                isSearchActive = false,
                selectedUsers = aListOfSelectedUsers(),
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState(isSearchActive = true),
            aUserListState(isSearchActive = true, searchQuery = "someone"),
            aUserListState(isSearchActive = true, searchQuery = "someone", selectionMode = SelectionMode.Multiple),
            aUserListState(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectedUsers = aMatrixUserList().toImmutableList(),
                searchResults = SearchBarResultState.Results(aListOfUserSearchResults()),
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectionMode = SelectionMode.Multiple,
                selectedUsers = aMatrixUserList().toImmutableList(),
                searchResults = SearchBarResultState.Results(aListOfUserSearchResults()),
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "something-with-no-results",
                searchResults = SearchBarResultState.NoResultsFound()
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "someone",
                selectionMode = SelectionMode.Single,
            ),
            aUserListState(
                recentDirectRooms = aRecentDirectRoomList(),
            ),
        )
}

fun aUserListState(
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<ImmutableList<UserSearchResult>> = SearchBarResultState.Initial(),
    selectedUsers: List<MatrixUser> = emptyList(),
    showSearchLoader: Boolean = false,
    selectionMode: SelectionMode = SelectionMode.Single,
    recentDirectRooms: List<RecentDirectRoom> = emptyList(),
    eventSink: (UserListEvents) -> Unit = {},
) = UserListState(
    isDebugBuild = false,
    searchQuery = searchQuery,
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    selectedUsers = selectedUsers.toImmutableList(),
    showSearchLoader = showSearchLoader,
    selectionMode = selectionMode,
    recentDirectRooms = recentDirectRooms.toImmutableList(),
    eventSink = eventSink
)

fun aListOfSelectedUsers() = aMatrixUserList().take(6).toImmutableList()
fun aListOfUserSearchResults() = aMatrixUserList().take(6).map { UserSearchResult(it) }.toImmutableList()

fun aRecentDirectRoomList(
    count: Int = 5
): List<RecentDirectRoom> = aMatrixUserList()
    .take(count)
    .map {
        RecentDirectRoom(RoomId("!aRoom:id"), it)
    }
