/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.api

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface LeaveRoomEvent {
    data class ShowConfirmation(val roomId: RoomId) : LeaveRoomEvent
    data object HideConfirmation : LeaveRoomEvent
    data class LeaveRoom(val roomId: RoomId) : LeaveRoomEvent
    data object HideError : LeaveRoomEvent
}
