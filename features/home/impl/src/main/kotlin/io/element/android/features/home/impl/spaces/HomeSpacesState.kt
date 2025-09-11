/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableSet

data class HomeSpacesState(
    val space: CurrentSpace,
    val spaceRooms: List<SpaceRoom>,
    val seenSpaceInvites: ImmutableSet<RoomId>,
    val hideInvitesAvatar: Boolean,
    val eventSink: (HomeSpacesEvents) -> Unit,
)

sealed interface CurrentSpace {
    object Root : CurrentSpace
    data class Space(val spaceRoom: SpaceRoom) : CurrentSpace
}
