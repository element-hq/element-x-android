/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.features.createroom.impl.userlist.aRecentDirectRoomList
import io.element.android.features.createroom.impl.userlist.aUserListState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.persistentListOf

open class CreateRoomRootStateProvider : PreviewParameterProvider<CreateRoomRootState> {
    override val values: Sequence<CreateRoomRootState>
        get() = sequenceOf(
            aCreateRoomRootState(),
            aCreateRoomRootState(
                startDmAction = AsyncAction.Loading,
                userListState = aMatrixUser().let {
                    aUserListState().copy(
                        searchQuery = it.userId.value,
                        searchResults = SearchBarResultState.Results(persistentListOf(UserSearchResult(it, false))),
                        selectedUsers = persistentListOf(it),
                        isSearchActive = true,
                    )
                }
            ),
            aCreateRoomRootState(
                startDmAction = AsyncAction.Failure(Throwable("error")),
                userListState = aMatrixUser().let {
                    aUserListState().copy(
                        searchQuery = it.userId.value,
                        searchResults = SearchBarResultState.Results(persistentListOf(UserSearchResult(it, false))),
                        selectedUsers = persistentListOf(it),
                        isSearchActive = true,
                    )
                }
            ),
            aCreateRoomRootState(
                userListState = aUserListState(
                    recentDirectRooms = aRecentDirectRoomList()
                )
            ),
        )
}

fun aCreateRoomRootState(
    applicationName: String = "Element X Preview",
    userListState: UserListState = aUserListState(),
    startDmAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    eventSink: (CreateRoomRootEvents) -> Unit = {},
) = CreateRoomRootState(
    isDebugBuild = false,
    applicationName = applicationName,
    userListState = userListState,
    startDmAction = startDmAction,
    eventSink = eventSink,
)
