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
import kotlinx.collections.immutable.ImmutableList

open class RoomListContentStateProvider : PreviewParameterProvider<RoomListContentState> {
    override val values: Sequence<RoomListContentState>
        get() = sequenceOf(
            aRoomsContentState(),
            aSkeletonContentState(),
            anEmptyContentState(),
            aMigrationContentState(),
        )
}

internal fun aRoomsContentState(
    invitesState: InvitesState = InvitesState.NoInvites,
    securityBannerState: SecurityBannerState = SecurityBannerState.None,
    summaries: ImmutableList<RoomListRoomSummary> = aRoomListRoomSummaryList(),
) = RoomListContentState.Rooms(
    invitesState = invitesState,
    securityBannerState = securityBannerState,
    summaries = summaries,
)

internal fun aMigrationContentState() = RoomListContentState.Migration

internal fun aSkeletonContentState() = RoomListContentState.Skeleton(16)

internal fun anEmptyContentState(
    invitesState: InvitesState = InvitesState.NoInvites,
) = RoomListContentState.Empty(invitesState)
