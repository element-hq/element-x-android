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

import android.view.Gravity
import androidx.compose.ui.graphics.Color

internal val DefaultMapUiSettings = MapUiSettings()

/**
 * Data class for UI-related settings on the map.
 *
 * Note: Should not be a data class if in need of maintaining binary compatibility
 * on future changes. See: https://jakewharton.com/public-api-challenges-in-kotlin/
 */
public data class MapUiSettings(
    public val compassEnabled: Boolean = true,
    public val rotationGesturesEnabled: Boolean = true,
    public val scrollGesturesEnabled: Boolean = true,
    public val tiltGesturesEnabled: Boolean = true,
    public val zoomGesturesEnabled: Boolean = true,
    public val logoGravity: Int = Gravity.BOTTOM,
    public val attributionGravity: Int = Gravity.BOTTOM,
    public val attributionTintColor: Color = Color.Unspecified,
)
