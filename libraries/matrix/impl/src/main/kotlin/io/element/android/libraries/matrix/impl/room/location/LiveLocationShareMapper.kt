/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import org.matrix.rustcomponents.sdk.LiveLocationShare as RustLiveLocationShare

fun RustLiveLocationShare.map(): LiveLocationShare {
    return LiveLocationShare(
        userId = UserId(userId),
        lastGeoUri = lastLocation.location.geoUri,
        lastTimestamp = lastLocation.ts.toLong(),
        isLive = isLive,
    )
}
