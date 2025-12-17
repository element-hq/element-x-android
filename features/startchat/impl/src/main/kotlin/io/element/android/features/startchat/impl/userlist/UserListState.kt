/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.userlist

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
