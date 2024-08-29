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
        )
}

internal fun aRoomsContentState(
    securityBannerState: SecurityBannerState = SecurityBannerState.None,
    summaries: ImmutableList<RoomListRoomSummary> = aRoomListRoomSummaryList(),
    fullScreenIntentPermissionsState: FullScreenIntentPermissionsState = aFullScreenIntentPermissionsState(),
) = RoomListContentState.Rooms(
    isDebugBuild = false,
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
