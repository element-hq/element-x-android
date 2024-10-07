/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class RoomListState(
    val matrixUser: MatrixUser,
    val showAvatarIndicator: Boolean,
    val hasNetworkConnection: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val contextMenu: ContextMenu,
    val leaveRoomState: LeaveRoomState,
    val filtersState: RoomListFiltersState,
    val searchState: RoomListSearchState,
    val contentState: RoomListContentState,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val directLogoutState: DirectLogoutState,
    val eventSink: (RoomListEvents) -> Unit,
) {
    val displayFilters = contentState is RoomListContentState.Rooms
    val displayActions = true

    sealed interface ContextMenu {
        data object Hidden : ContextMenu
        data class Shown(
            val roomId: RoomId,
            val roomName: String?,
            val isDm: Boolean,
            val isFavorite: Boolean,
            val markAsUnreadFeatureFlagEnabled: Boolean,
            val hasNewContent: Boolean,
        ) : ContextMenu
    }
}

enum class SecurityBannerState {
    None,
    SetUpRecovery,
    RecoveryKeyConfirmation,
    NeedsNativeSlidingSyncMigration,
}

@Immutable
sealed interface RoomListContentState {
    data class Skeleton(val count: Int) : RoomListContentState
    data object Empty : RoomListContentState
    data class Rooms(
        val isDebugBuild: Boolean,
        val securityBannerState: SecurityBannerState,
        val fullScreenIntentPermissionsState: FullScreenIntentPermissionsState,
        val summaries: ImmutableList<RoomListRoomSummary>,
    ) : RoomListContentState
}
