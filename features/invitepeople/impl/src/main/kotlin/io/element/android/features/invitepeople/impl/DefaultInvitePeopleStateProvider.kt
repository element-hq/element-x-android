/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
                        InvitableUser(aMatrixUser("@alice:server.org")),
                        InvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                        InvitableUser(aMatrixUser("@carol:server.org", "Carol"), isSelected = true),
                        InvitableUser(aMatrixUser("@eve:server.org", "Eve"), isSelected = true, isAlreadyJoined = true),
                        InvitableUser(aMatrixUser("@justin:server.org", "Justin"), isSelected = true, isAlreadyInvited = true),
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
                        InvitableUser(aMatrixUser("@alice:server.org"), isUnresolved = true),
                        InvitableUser(aMatrixUser("@bob:server.org", "Bob")),
                    )
                )
            ),
            aDefaultInvitePeopleState(
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

private fun aDefaultInvitePeopleState(
    canInvite: Boolean = false,
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<InvitableUser>> = SearchBarResultState.Initial(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    isSearchActive: Boolean = false,
    showSearchLoader: Boolean = false,
): DefaultInvitePeopleState {
    return DefaultInvitePeopleState(
        canInvite = canInvite,
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedUsers = selectedUsers,
        isSearchActive = isSearchActive,
        showSearchLoader = showSearchLoader,
        eventSink = {},
    )
}
