/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import io.element.android.features.home.impl.roomlist.RoomListState
import io.element.android.features.home.impl.spaces.HomeSpacesState
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class HomeState(
    /**
     * The current user of this session, in case of multiple accounts, will contains 3 items, with the
     * current user in the middle.
     */
    val currentUserAndNeighbors: ImmutableList<MatrixUser>,
    val showAvatarIndicator: Boolean,
    val hasNetworkConnection: Boolean,
    val currentHomeNavigationBarItem: HomeNavigationBarItem,
    val roomListState: RoomListState,
    val homeSpacesState: HomeSpacesState,
    val snackbarMessage: SnackbarMessage?,
    val canReportBug: Boolean,
    val directLogoutState: DirectLogoutState,
    val isSpaceFeatureEnabled: Boolean,
    val eventSink: (HomeEvents) -> Unit,
) {
    val displayActions = currentHomeNavigationBarItem == HomeNavigationBarItem.Chats
    val displayRoomListFilters = currentHomeNavigationBarItem == HomeNavigationBarItem.Chats && roomListState.displayFilters
    val showNavigationBar = isSpaceFeatureEnabled && homeSpacesState.spaceRooms.isNotEmpty()
}
