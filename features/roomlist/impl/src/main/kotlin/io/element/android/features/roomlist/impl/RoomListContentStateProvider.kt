/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class RoomListContentStateProvider : PreviewParameterProvider<RoomListContentState> {
    override val values: Sequence<RoomListContentState>
        get() = sequenceOf(
            aRoomsContentState(),
            aRoomsContentState(summaries = persistentListOf()),
            aSkeletonContentState(),
            anEmptyContentState(),
            aRoomsContentState(securityBannerState = SecurityBannerState.NeedsNativeSlidingSyncMigration),
        )
}

internal fun aRoomsContentState(
    securityBannerState: SecurityBannerState = SecurityBannerState.None,
    summaries: ImmutableList<RoomListRoomSummary> = aRoomListRoomSummaryList(),
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
) = RoomListContentState.Rooms(
    securityBannerState = securityBannerState,
    fullScreenIntentPermissionsState = fullScreenIntentPermissionsState,
    summaries = summaries,
)

internal fun aSkeletonContentState() = RoomListContentState.Skeleton(16)

internal fun anEmptyContentState() = RoomListContentState.Empty

internal fun aFullScreenIntentPermissionsState(
    permissionGranted: Boolean = true,
    shouldDisplay: Boolean = false,
    openFullScreenIntentSettings: () -> Unit = {},
    dismissFullScreenIntentBanner: () -> Unit = {},
) = FullScreenIntentPermissionsState(
    permissionGranted = permissionGranted,
    shouldDisplayBanner = shouldDisplay,
    openFullScreenIntentSettings = openFullScreenIntentSettings,
    dismissFullScreenIntentBanner = dismissFullScreenIntentBanner,
)
