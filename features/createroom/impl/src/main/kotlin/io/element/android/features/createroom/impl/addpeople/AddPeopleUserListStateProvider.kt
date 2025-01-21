/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.createroom.impl.userlist.SelectionMode
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.features.createroom.impl.userlist.aRecentDirectRoomList
import io.element.android.features.createroom.impl.userlist.aUserListState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.toImmutableList

open class AddPeopleUserListStateProvider : PreviewParameterProvider<UserListState> {
    override val values: Sequence<UserListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState(
                searchResults = SearchBarResultState.Results(aMatrixUserList().toImmutableList()),
                selectedUsers = aMatrixUserList().toImmutableList(),
                isSearchActive = false,
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState(
                searchResults = SearchBarResultState.Results(
                    aMatrixUserList()
                        .mapIndexed { index, matrixUser ->
                            UserSearchResult(matrixUser, index % 2 == 0)
                        }
                        .toImmutableList()
                ),
                selectedUsers = aMatrixUserList().toImmutableList(),
                isSearchActive = true,
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState(
                recentDirectRooms = aRecentDirectRoomList(),
            ),
        )
}
