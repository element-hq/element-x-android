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
