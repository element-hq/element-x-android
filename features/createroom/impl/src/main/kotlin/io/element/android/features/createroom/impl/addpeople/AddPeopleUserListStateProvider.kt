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
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListState
import io.element.android.features.userlist.api.aListOfSelectedUsers
import io.element.android.features.userlist.api.aUserListState
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toImmutableList

open class AddPeopleUserListStateProvider : PreviewParameterProvider<UserListState> {
    override val values: Sequence<UserListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState().copy(
                searchResults = aMatrixUserList().toImmutableList(),
                selectedUsers = aListOfSelectedUsers(),
                isSearchActive = false,
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState().copy(
                searchResults = aMatrixUserList().toImmutableList(),
                selectedUsers = aListOfSelectedUsers(),
                isSearchActive = true,
                selectionMode = SelectionMode.Multiple,
            )
        )
}
