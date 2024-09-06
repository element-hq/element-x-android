/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import kotlinx.collections.immutable.ImmutableList

data class ChangeRoomPermissionsState(
    val section: ChangeRoomPermissionsSection,
    val currentPermissions: MatrixRoomPowerLevels?,
    val items: ImmutableList<RoomPermissionType>,
    val hasChanges: Boolean,
    val saveAction: AsyncAction<Unit>,
    val confirmExitAction: AsyncAction<Unit>,
    val eventSink: (ChangeRoomPermissionsEvent) -> Unit,
)

enum class RoomPermissionType {
    BAN,
    INVITE,
    KICK,
    SEND_EVENTS,
    REDACT_EVENTS,
    ROOM_NAME,
    ROOM_AVATAR,
    ROOM_TOPIC
}
