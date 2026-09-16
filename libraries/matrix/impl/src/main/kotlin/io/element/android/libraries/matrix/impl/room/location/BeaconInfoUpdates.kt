/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.location.BeaconInfoUpdate
import org.matrix.rustcomponents.sdk.BeaconInfoUpdate as RustBeaconInfoUpdate

fun RustBeaconInfoUpdate.map(): BeaconInfoUpdate {
    return BeaconInfoUpdate(
        roomId = RoomId(roomId),
        beaconId = EventId(eventId),
        isLive = live
    )
}
