/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.location

import io.element.android.libraries.matrix.api.core.UserId

data class Location(
    val body: String?,
    val geoUri: String,
    val description: String?,
    val zoomLevel: Int?,
)

data class LastLocation(
    val location: Location,
    val ts: ULong,
)

data class LiveLocationShare(
    val userId: UserId,
    val lastLocation: LastLocation,
    val isLive: Boolean,
)
