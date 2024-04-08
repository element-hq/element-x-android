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

package io.element.android.features.createroom.impl.userlist

import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.recent.RecentDirectRoom
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList

data class UserListState(
    val searchQuery: String,
    val searchResults: SearchBarResultState<ImmutableList<UserSearchResult>>,
    val showSearchLoader: Boolean,
    val selectedUsers: ImmutableList<MatrixUser>,
    val isSearchActive: Boolean,
    val selectionMode: SelectionMode,
    val recentDirectRooms: ImmutableList<RecentDirectRoom>,
    val eventSink: (UserListEvents) -> Unit,
) {
    val isMultiSelectionEnabled = selectionMode == SelectionMode.Multiple
}
