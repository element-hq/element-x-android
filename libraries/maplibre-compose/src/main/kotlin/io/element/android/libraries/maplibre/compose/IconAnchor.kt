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
import org.maplibre.android.style.layers.Property

@Immutable
public enum class IconAnchor {
    CENTER,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    @Property.ICON_ANCHOR
    internal fun toInternal(): String = when (this) {
        CENTER -> Property.ICON_ANCHOR_CENTER
        LEFT -> Property.ICON_ANCHOR_LEFT
        RIGHT -> Property.ICON_ANCHOR_RIGHT
        TOP -> Property.ICON_ANCHOR_TOP
        BOTTOM -> Property.ICON_ANCHOR_BOTTOM
        TOP_LEFT -> Property.ICON_ANCHOR_TOP_LEFT
        TOP_RIGHT -> Property.ICON_ANCHOR_TOP_RIGHT
        BOTTOM_LEFT -> Property.ICON_ANCHOR_BOTTOM_LEFT
        BOTTOM_RIGHT -> Property.ICON_ANCHOR_BOTTOM_RIGHT
    }
}
