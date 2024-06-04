/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright 2021 Google LLC
 * Copied and adapted from android-maps-compose (https://github.com/googlemaps/android-maps-compose)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
