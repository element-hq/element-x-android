/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.Immutable
import org.maplibre.android.location.modes.CameraMode as InternalCameraMode

@Immutable
public enum class CameraMode {
    NONE,
    NONE_COMPASS,
    NONE_GPS,
    TRACKING,
    TRACKING_COMPASS,
    TRACKING_GPS,
    TRACKING_GPS_NORTH;

    @InternalCameraMode.Mode
    internal fun toInternal(): Int = when (this) {
        NONE -> InternalCameraMode.NONE
        NONE_COMPASS -> InternalCameraMode.NONE_COMPASS
        NONE_GPS -> InternalCameraMode.NONE_GPS
        TRACKING -> InternalCameraMode.TRACKING
        TRACKING_COMPASS -> InternalCameraMode.TRACKING_COMPASS
        TRACKING_GPS -> InternalCameraMode.TRACKING_GPS
        TRACKING_GPS_NORTH -> InternalCameraMode.TRACKING_GPS_NORTH
    }

    internal companion object {
        fun fromInternal(@InternalCameraMode.Mode mode: Int): CameraMode = when (mode) {
            InternalCameraMode.NONE -> NONE
            InternalCameraMode.NONE_COMPASS -> NONE_COMPASS
            InternalCameraMode.NONE_GPS -> NONE_GPS
            InternalCameraMode.TRACKING -> TRACKING
            InternalCameraMode.TRACKING_COMPASS -> TRACKING_COMPASS
            InternalCameraMode.TRACKING_GPS -> TRACKING_GPS
            InternalCameraMode.TRACKING_GPS_NORTH -> TRACKING_GPS_NORTH
            else -> error("Unknown camera mode: $mode")
        }
    }
}
