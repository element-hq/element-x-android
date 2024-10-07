/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            aRoomInviteMembersState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        InvitableUser(aMatrixUser("@alice:server.org"), isUnresolved = true),
                    )
                ),
                showSearchLoader = true,
            ),
        )
}

private fun aRoomInviteMembersState(
    canInvite: Boolean = false,
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<InvitableUser>> = SearchBarResultState.Initial(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    isSearchActive: Boolean = false,
    showSearchLoader: Boolean = false,
): RoomInviteMembersState {
    return RoomInviteMembersState(
        isDebugBuild = false,
        canInvite = canInvite,
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedUsers = selectedUsers,
        isSearchActive = isSearchActive,
        showSearchLoader = showSearchLoader,
        eventSink = {},
    )
}
