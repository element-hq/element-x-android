/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

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
