/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
import io.element.android.features.location.api.Location
import io.element.android.libraries.matrix.api.location.LiveLocationShare
import org.maplibre.android.geometry.LatLng
import org.ramani.compose.CircleWithItem

@Composable
fun LocationSymbol(item: LiveLocationShare, textColor: String) {
    val location = Location.fromGeoUri(item.lastLocation.location.geoUri) ?: return
    val latLng = LatLng(location.lat, location.lon)

//    CustomCircleWithItem(
//        center = latLng,
//        radius = 10.0F,
//        isDraggable = false,
//        color = "#F6993A",
//        text = item.userId.toString(),
//        zIndex = 1,
//        itemSize = 12F,
//        borderColor = "#4A4A4A",
//        borderWidth = 3.0F,
//        imageId = null,
//        textColor = textColor
//    )
    CircleWithItem(
        center = latLng,
        radius = 10.0F,
        isDraggable = false,
        color = "#F6993A",
        text = "HE",
        zIndex = 1,
        itemSize = 12F,
        borderColor = "#4A4A4A",
        borderWidth = 3.0F,
        imageId = null,
    )
}
