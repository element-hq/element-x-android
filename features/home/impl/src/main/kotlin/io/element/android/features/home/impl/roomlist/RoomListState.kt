/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import androidx.compose.runtime.Immutable
import io.element.android.features.home.impl.filters.RoomListFiltersState
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.search.GlobalSearchState
import io.element.android.features.home.impl.search.RoomListSearchState
import io.element.android.features.home.impl.spacefilters.SpaceFiltersState
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.battery.BatteryOptimizationState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

data class RoomListState(
    val contextMenu: ContextMenu,
    val declineInviteMenu: DeclineInviteMenu,
    val leaveRoomState: LeaveRoomState,
    val filtersState: RoomListFiltersState,
    val searchState: RoomListSearchState,
    val globalSearchState: GlobalSearchState,
    val spaceFiltersState: SpaceFiltersState,
    val contentState: RoomListContentState,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val hideInvitesAvatars: Boolean,
    val canReportRoom: Boolean,
    val eventSink: (RoomListEvent) -> Unit,
) {
    val displayFilters = contentState is RoomListContentState.Rooms

    sealed interface ContextMenu {
        data object Hidden : ContextMenu
        data class Shown(
            val roomId: RoomId,
            val roomName: String?,
            val isDm: Boolean,
            val isFavorite: Boolean,
            val hasNewContent: Boolean,
        ) : ContextMenu
    }

    sealed interface DeclineInviteMenu {
        data object Hidden : DeclineInviteMenu
        data class Shown(val roomSummary: RoomListRoomSummary) : DeclineInviteMenu
    }
}

enum class SecurityBannerState {
    None,
    SetUpRecovery,
    RecoveryKeyConfirmation,
}

/**
 * The banners which can be displayed at the top of the room list. At most one banner is displayed at a time.
 */
internal enum class RoomListBanner {
    SetUpRecovery,
    RecoveryKeyConfirmation,
    FullScreenIntentPermission,
    BatteryOptimization,
    NewNotificationSound,
    SwipeAvatar,
}

@Immutable
sealed interface RoomListContentState {
    data class Skeleton(val count: Int) : RoomListContentState
    data class Empty(
        val securityBannerState: SecurityBannerState,
    ) : RoomListContentState

    data class Rooms(
        val securityBannerState: SecurityBannerState,
        val fullScreenIntentPermissionsState: FullScreenIntentPermissionsState,
        val batteryOptimizationState: BatteryOptimizationState,
        val showNewNotificationSoundBanner: Boolean,
        val showSwipeAvatarBanner: Boolean,
        val showUnreadCount: Boolean,
        val summaries: ImmutableList<RoomListRoomSummary>,
        val seenRoomInvites: ImmutableSet<RoomId>,
        val showAllActivity: Boolean,
    ) : RoomListContentState
}

internal fun SecurityBannerState.toRoomListBanner(): RoomListBanner? = when (this) {
    SecurityBannerState.SetUpRecovery -> RoomListBanner.SetUpRecovery
    SecurityBannerState.RecoveryKeyConfirmation -> RoomListBanner.RecoveryKeyConfirmation
    SecurityBannerState.None -> null
}

/**
 * Banner precedence (top-to-bottom): security > full-screen-intent > battery-optimization >
 * new-notification-sound > swipe-avatar. At most one renders at a time.
 */
internal fun RoomListContentState.Rooms.toRoomListBanner(): RoomListBanner? {
    return securityBannerState.toRoomListBanner() ?: when {
        fullScreenIntentPermissionsState.shouldDisplayBanner -> RoomListBanner.FullScreenIntentPermission
        batteryOptimizationState.shouldDisplayBanner -> RoomListBanner.BatteryOptimization
        showNewNotificationSoundBanner -> RoomListBanner.NewNotificationSound
        showSwipeAvatarBanner -> RoomListBanner.SwipeAvatar
        else -> null
    }
}

