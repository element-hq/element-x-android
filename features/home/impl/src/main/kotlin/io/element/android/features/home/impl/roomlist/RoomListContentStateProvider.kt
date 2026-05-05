/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.api.battery.BatteryOptimizationState
import io.element.android.libraries.push.api.battery.aBatteryOptimizationState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet

open class RoomListContentStateProvider : PreviewParameterProvider<RoomListContentState> {
    override val values: Sequence<RoomListContentState>
        get() = sequenceOf(
            aRoomsContentState(),
            aRoomsContentState(summaries = persistentListOf()),
            aSkeletonContentState(),
            anEmptyContentState(),
            anEmptyContentState(securityBannerState = SecurityBannerState.SetUpRecovery),
            aRoomsContentState(
                showNewNotificationSoundBanner = true,
            ),
        )
}

internal fun aRoomsContentState(
    securityBannerState: SecurityBannerState = SecurityBannerState.None,
    showNewNotificationSoundBanner: Boolean = false,
    summaries: ImmutableList<RoomListRoomSummary> = aRoomListRoomSummaryList(),
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
    batteryOptimizationState: BatteryOptimizationState = aBatteryOptimizationState(),
    seenRoomInvites: Set<RoomId> = emptySet(),
) = RoomListContentState.Rooms(
    securityBannerState = securityBannerState,
    showNewNotificationSoundBanner = showNewNotificationSoundBanner,
    fullScreenIntentPermissionsState = fullScreenIntentPermissionsState,
    batteryOptimizationState = batteryOptimizationState,
    summaries = summaries,
    seenRoomInvites = seenRoomInvites.toImmutableSet(),
)

internal fun aSkeletonContentState() = RoomListContentState.Skeleton(16)

internal fun anEmptyContentState(
    securityBannerState: SecurityBannerState = SecurityBannerState.None,
) = RoomListContentState.Empty(
    securityBannerState = securityBannerState,
)
