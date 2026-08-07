/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

/**
 * Permissions to send beacon and beacon_info events in the room.
 */
data class SendLiveLocationPermissions(
    val canSendBeacon: Boolean,
    val canSendBeaconInfo: Boolean,
) {
    val hasAll = canSendBeaconInfo && canSendBeacon

    companion object {
        val DEFAULT = SendLiveLocationPermissions(canSendBeacon = false, canSendBeaconInfo = false)
        val GRANTED = SendLiveLocationPermissions(canSendBeacon = true, canSendBeaconInfo = true)
    }
}

fun RoomPermissions.sendLiveLocationPermissions(): SendLiveLocationPermissions {
    return SendLiveLocationPermissions(
        canSendBeaconInfo = canOwnUserSendState(StateEventType.BeaconInfo),
        canSendBeacon = canOwnUserSendMessage(MessageEventType.Beacon),
    )
}
