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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.userlist.api.anUserListState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal class RoomMemberListStateProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(allUsers = Async.Success(persistentListOf(aMatrixUser()))),
            aRoomMemberListState(allUsers = Async.Loading())
        )
}

internal fun aRoomMemberListState(
    searchResults: ImmutableList<MatrixUser> = persistentListOf(),
    allUsers: Async<ImmutableList<MatrixUser>> = Async.Uninitialized,
) =
    RoomMemberListState(
        userListState = anUserListState().copy(searchResults = searchResults),
        allUsers = allUsers,
    )
