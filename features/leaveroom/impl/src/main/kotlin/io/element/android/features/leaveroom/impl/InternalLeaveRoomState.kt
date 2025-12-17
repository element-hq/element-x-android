/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.leaveroom.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

data class InternalLeaveRoomState(
    val leaveAction: AsyncAction<Unit>,
    override val eventSink: (LeaveRoomEvent) -> Unit
) : LeaveRoomState

@Immutable
sealed interface Confirmation : AsyncAction.Confirming {
    data class Dm(val roomId: RoomId) : Confirmation
    data class Generic(val roomId: RoomId) : Confirmation
    data class PrivateRoom(val roomId: RoomId) : Confirmation
    data class LastUserInRoom(val roomId: RoomId) : Confirmation
    data class LastOwnerInRoom(val roomId: RoomId) : Confirmation
}
