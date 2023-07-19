/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.location.impl

import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import io.element.android.libraries.maplibre.compose.MapLocationSettings
import io.element.android.libraries.maplibre.compose.MapSymbolManagerSettings
import io.element.android.libraries.maplibre.compose.MapUiSettings
import io.element.android.libraries.theme.ElementTheme

/**
 * Common configuration values for the map.
 */
object MapDefaults {
    val uiSettings: MapUiSettings
        @Composable
        @ReadOnlyComposable
        get() = MapUiSettings(
            compassEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true,
            logoGravity = Gravity.TOP,
            attributionGravity = Gravity.TOP,
            attributionTintColor = ElementTheme.colors.iconPrimary
        )

    val symbolManagerSettings: MapSymbolManagerSettings
        get() = MapSymbolManagerSettings(
            iconAllowOverlap = true
        )

    val locationSettings: MapLocationSettings
        get() = MapLocationSettings(
            locationEnabled = false,
        )

    val centerCameraPosition = CameraPosition.Builder()
        .target(LatLng(49.843, 9.902056))
        .zoom(2.7)
        .build()
}
