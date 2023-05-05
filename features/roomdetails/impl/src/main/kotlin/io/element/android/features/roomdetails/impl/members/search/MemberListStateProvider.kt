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

package io.element.android.features.roomdetails.impl.members.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class MemberListStateProvider : PreviewParameterProvider<MemberListState> {
    override val values: Sequence<MemberListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState().copy(
                isSearchActive = false,
                selectedUsers = aListOfSelectedUsers(),
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState().copy(isSearchActive = true),
            aUserListState().copy(isSearchActive = true, searchQuery = "someone"),
            aUserListState().copy(isSearchActive = true, searchQuery = "someone", selectionMode = SelectionMode.Multiple),
            aUserListState().copy(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectedUsers = aListOfSelectedUsers(),
                searchResults = UserSearchResultState.Results(aMatrixUserList().toImmutableList()),
            ),
            aUserListState().copy(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectionMode = SelectionMode.Multiple,
                selectedUsers = aListOfSelectedUsers(),
                searchResults = UserSearchResultState.Results(aMatrixUserList().toImmutableList()),
            ),
            aUserListState().copy(
                isSearchActive = true,
                searchQuery = "something-with-no-results",
                searchResults = UserSearchResultState.NoResults
            ),
        )
}

fun aUserListState() = MemberListState(
    isSearchActive = false,
    searchQuery = "",
    searchResults = UserSearchResultState.NotSearching,
    selectedUsers = persistentListOf(),
    selectionMode = SelectionMode.Single,
    eventSink = {}
)

fun aListOfSelectedUsers() = aMatrixUserList().take(6).toImmutableList()
