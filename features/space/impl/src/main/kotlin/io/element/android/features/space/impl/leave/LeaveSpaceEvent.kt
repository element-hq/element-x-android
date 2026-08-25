/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface LeaveSpaceEvent {
    data object Retry : LeaveSpaceEvent
    data object SelectAllRooms : LeaveSpaceEvent
    data object DeselectAllRooms : LeaveSpaceEvent
    data class ToggleRoomSelection(val roomId: RoomId) : LeaveSpaceEvent
    data object LeaveSpace : LeaveSpaceEvent
    data object CloseError : LeaveSpaceEvent
}
