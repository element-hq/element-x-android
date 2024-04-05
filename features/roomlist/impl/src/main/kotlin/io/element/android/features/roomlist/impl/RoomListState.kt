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

package io.element.android.features.roomlist.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
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
    val eventSink: (RoomListEvents) -> Unit,
) {
    val displayFilters = filtersState.isFeatureEnabled && contentState is RoomListContentState.Rooms
    val displayActions = contentState !is RoomListContentState.Migration

    sealed interface ContextMenu {
        data object Hidden : ContextMenu
        data class Shown(
            val roomId: RoomId,
            val roomName: String,
            val isDm: Boolean,
            val isFavorite: Boolean,
            val markAsUnreadFeatureFlagEnabled: Boolean,
            val hasNewContent: Boolean,
        ) : ContextMenu
    }
}

enum class InvitesState {
    NoInvites,
    SeenInvites,
    NewInvites,
}

enum class SecurityBannerState {
    None,
    RecoveryKeyConfirmation,
}

@Immutable
sealed interface RoomListContentState {
    data object Migration : RoomListContentState
    data class Skeleton(val count: Int) : RoomListContentState
    data class Empty(val invitesState: InvitesState) : RoomListContentState
    data class Rooms(
        val invitesState: InvitesState,
        val securityBannerState: SecurityBannerState,
        val summaries: ImmutableList<RoomListRoomSummary>,
    ) : RoomListContentState
}
