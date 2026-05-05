/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.model.aRoomListRoomSummary

open class RoomListStateDeclineInviteMenuShownProvider : PreviewParameterProvider<RoomListState.DeclineInviteMenu.Shown> {
    override val values: Sequence<RoomListState.DeclineInviteMenu.Shown>
        get() = sequenceOf(
            aDeclineInviteMenuShown(),
            aDeclineInviteMenuShown(
                aRoomListRoomSummary(
                    name = LoremIpsum(500).values.first(),
                )
            ),
            aDeclineInviteMenuShown(
                aRoomListRoomSummary(
                    name = null,
                )
            ),
        )
}

internal fun aDeclineInviteMenuShown(
    roomSummary: RoomListRoomSummary = aRoomListRoomSummary(),
) = RoomListState.DeclineInviteMenu.Shown(
    roomSummary = roomSummary,
)
