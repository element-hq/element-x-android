/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
import org.maplibre.android.geometry.LatLng
import org.ramani.compose.CircleWithItem

@Composable
fun LocationSymbol(item: MapRealtimeLocationDot) {
    val latLng = LatLng(item.location.lat, item.location.lon)

    CircleWithItem(
        center = latLng,
        radius = 10.0F,
        isDraggable = false,
        color = "#F6993A",
        text = item.userName,
        zIndex = 1,
        itemSize = 12F,
        borderColor = "#4A4A4A",
        borderWidth = 3.0F,
    )
}
