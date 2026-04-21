/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.location

import io.element.android.libraries.matrix.api.core.UserId

/**
 * Represents a live location share from a user in a room.
 */
data class LiveLocationShare(
    /** The user who is sharing their location. */
    val userId: UserId,
    /** The last known location if any. */
    val lastLocation: LastLocation?,
    /** The timestamp when location sharing started, in milliseconds.*/
    val startTimestamp: Long,
    /** The timestamp when location sharing ends, in milliseconds. */
    val endTimestamp: Long,
)

data class LastLocation(
    /** The last known geo URI (e.g., "geo:51.5074,-0.1278"). */
    val geoUri: String,
    /** The timestamp of the last location update. */
    val timestamp: Long,
    /** The asset of the last location update. */
    val assetType: AssetType,
)
