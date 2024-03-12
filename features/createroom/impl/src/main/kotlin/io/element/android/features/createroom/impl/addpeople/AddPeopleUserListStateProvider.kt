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

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.createroom.impl.userlist.SelectionMode
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.features.createroom.impl.userlist.aUserListState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.toImmutableList

open class AddPeopleUserListStateProvider : PreviewParameterProvider<UserListState> {
    override val values: Sequence<UserListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState().copy(
                searchResults = SearchBarResultState.Results(aMatrixUserList().toImmutableList()),
                selectedUsers = aMatrixUserList().toImmutableList(),
                isSearchActive = false,
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState().copy(
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
            )
        )
}
