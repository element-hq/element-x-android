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
