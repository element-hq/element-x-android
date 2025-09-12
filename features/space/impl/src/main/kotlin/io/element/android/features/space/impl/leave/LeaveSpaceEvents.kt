/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface LeaveSpaceEvents {
    data object SelectAllRooms : LeaveSpaceEvents
    data object DeselectAllRooms : LeaveSpaceEvents
    data class ToggleRoomSelection(val roomId: RoomId) : LeaveSpaceEvents
    data object LeaveSpace : LeaveSpaceEvents
    data object CloseError : LeaveSpaceEvents
}
