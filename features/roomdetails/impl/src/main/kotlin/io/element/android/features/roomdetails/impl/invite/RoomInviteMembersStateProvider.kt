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

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class RoomInviteMembersStateProvider : PreviewParameterProvider<RoomInviteMembersState> {
    override val values: Sequence<RoomInviteMembersState>
        get() = sequenceOf(
            aRoomInviteMembersState(),
            aRoomInviteMembersState(canInvite = true, selectedUsers = aMatrixUserList().toImmutableList()),
            aRoomInviteMembersState(isSearchActive = true, searchQuery = "some query"),
            aRoomInviteMembersState(isSearchActive = true, searchQuery = "some query", selectedUsers = aMatrixUserList().toImmutableList()),
            aRoomInviteMembersState(isSearchActive = true, searchQuery = "some query", searchResults = SearchBarResultState.NoResultsFound()),
            aRoomInviteMembersState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "some query",
                selectedUsers = persistentListOf(
                    aMatrixUser("@carol:server.org", "Carol")
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        InvitableUser(aMatrixUser("@alice:server.org")),
                        InvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                        InvitableUser(aMatrixUser("@carol:server.org", "Carol"), isSelected = true),
                        InvitableUser(aMatrixUser("@eve:server.org", "Eve"), isSelected = true, isAlreadyJoined = true),
                        InvitableUser(aMatrixUser("@justin:server.org", "Justin"), isSelected = true, isAlreadyInvited = true),
                    )
                )
            ),
            aRoomInviteMembersState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                selectedUsers = persistentListOf(
                    aMatrixUser("@carol:server.org", "Carol")
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        InvitableUser(aMatrixUser("@alice:server.org"), isUnresolved = true),
                        InvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                    )
                )
            ),
        )
}

private fun aRoomInviteMembersState(
    canInvite: Boolean = false,
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<InvitableUser>> = SearchBarResultState.Empty(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    isSearchActive: Boolean = false,
    isFetchingSearchResults: Boolean = false,
): RoomInviteMembersState {
    return RoomInviteMembersState(
        canInvite = canInvite,
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedUsers = selectedUsers,
        isSearchActive = isSearchActive,
        isFetchingSearchResults = isFetchingSearchResults,
        eventSink = {},
    )
}
