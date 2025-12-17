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
import org.maplibre.android.maps.MapLibreMap.OnCameraMoveStartedListener.REASON_API_ANIMATION
import org.maplibre.android.maps.MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE
import org.maplibre.android.maps.MapLibreMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION

/**
 * Enumerates the different reasons why the map camera started to move.
 *
 * Based on enum values from https://docs.maptiler.com/maplibre-gl-native-android/org.maplibre.android.maps/#oncameramovestartedlistener.
 *
 * [NO_MOVEMENT_YET] is used as the initial state before any map movement has been observed.
 *
 * [UNKNOWN] is used to represent when an unsupported integer value is provided to [fromInt] - this
 * may be a new constant value from the Maps SDK that isn't supported by maps-compose yet, in which
 * case this library should be updated to include a new enum value for that constant.
 */
@Immutable
public enum class CameraMoveStartedReason(public val value: Int) {
    UNKNOWN(-2),
    NO_MOVEMENT_YET(-1),
    GESTURE(REASON_API_GESTURE),
    API_ANIMATION(REASON_API_ANIMATION),
    DEVELOPER_ANIMATION(REASON_DEVELOPER_ANIMATION);

    public companion object {
        /**
         * Converts from the Maps SDK [org.maplibre.android.maps.MapLibreMap.OnCameraMoveStartedListener]
         * constants to [CameraMoveStartedReason], or returns [UNKNOWN] if there is no such
         * [CameraMoveStartedReason] for the given [value].
         *
         * See https://docs.maptiler.com/maplibre-gl-native-android/org.maplibre.android.maps/#oncameramovestartedlistener.
         */
        public fun fromInt(value: Int): CameraMoveStartedReason {
            return values().firstOrNull { it.value == value } ?: return UNKNOWN
        }
    }
}
