/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class DefaultInvitePeopleStateProvider : PreviewParameterProvider<DefaultInvitePeopleState> {
    override val values: Sequence<DefaultInvitePeopleState>
        get() = sequenceOf(
            aDefaultInvitePeopleState(),
            aDefaultInvitePeopleState(canInvite = true, selectedUsers = aMatrixUserList().toImmutableList()),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query"),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query", selectedUsers = aMatrixUserList().toImmutableList()),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query", searchResults = SearchBarResultState.NoResultsFound()),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "some query",
                selectedUsers = persistentListOf(
                    aMatrixUser("@carol:server.org", "Carol")
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser("@alice:server.org")),
                        anInvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                        anInvitableUser(aMatrixUser("@carol:server.org", "Carol"), isSelected = true),
                        anInvitableUser(aMatrixUser("@eve:server.org", "Eve"), isSelected = true, isAlreadyJoined = true),
                        anInvitableUser(aMatrixUser("@justin:server.org", "Justin"), isSelected = true, isAlreadyInvited = true),
                    )
                )
            ),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                selectedUsers = persistentListOf(
                    aMatrixUser("@carol:server.org", "Carol")
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser("@alice:server.org"), isUnresolved = true),
                        anInvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                    )
                )
            ),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser("@alice:server.org"), isUnresolved = true),
                    )
                ),
                showSearchLoader = true,
            ),
            aDefaultInvitePeopleState(room = AsyncData.Failure(Exception("Room not found"))),
            aDefaultInvitePeopleState(
                canInvite = false,
                selectedUsers = aMatrixUserList().toImmutableList(),
                sendInvitesAction = AsyncAction.Loading,
            ),
        )
}

private fun anInvitableUser(
    matrixUser: MatrixUser,
    isSelected: Boolean = false,
    isAlreadyJoined: Boolean = false,
    isAlreadyInvited: Boolean = false,
    isUnresolved: Boolean = false,
) = InvitableUser(
    matrixUser = matrixUser,
    isSelected = isSelected,
    isAlreadyJoined = isAlreadyJoined,
    isAlreadyInvited = isAlreadyInvited,
    isUnresolved = isUnresolved,
)

private fun aDefaultInvitePeopleState(
    room: AsyncData<Unit> = AsyncData.Success(Unit),
    canInvite: Boolean = false,
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<InvitableUser>> = SearchBarResultState.Initial(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    isSearchActive: Boolean = false,
    showSearchLoader: Boolean = false,
    sendInvitesAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
): DefaultInvitePeopleState {
    return DefaultInvitePeopleState(
        room = room,
        canInvite = canInvite,
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedUsers = selectedUsers,
        isSearchActive = isSearchActive,
        showSearchLoader = showSearchLoader,
        sendInvitesAction = sendInvitesAction,
        eventSink = {},
    )
}
