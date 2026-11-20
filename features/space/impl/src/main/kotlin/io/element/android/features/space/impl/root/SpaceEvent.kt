/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom

sealed interface SpaceEvent {
    data object LoadMore : SpaceEvent
    data class Join(val spaceRoom: SpaceRoom) : SpaceEvent
    data object ClearFailures : SpaceEvent
    data class AcceptInvite(val spaceRoom: SpaceRoom) : SpaceEvent
    data class DeclineInvite(val spaceRoom: SpaceRoom) : SpaceEvent

    data class ShowTopicViewer(val topic: String) : SpaceEvent
    data object HideTopicViewer : SpaceEvent

    // Manage mode events
    data object EnterManageMode : SpaceEvent
    data object ExitManageMode : SpaceEvent
    data class ToggleRoomSelection(val roomId: RoomId) : SpaceEvent
    data object ConfirmRoomRemoval : SpaceEvent
    data object RemoveSelectedRooms : SpaceEvent
    data object ClearRemoveAction : SpaceEvent
}
