/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.maplibre.compose.Circle
import io.element.android.libraries.maplibre.compose.TextSymbol
import org.maplibre.android.geometry.LatLng

@Composable
fun UpdateCenter(coord: LatLng, centerUpdated: (LatLng) -> Unit) {
    centerUpdated(coord)
}

@Composable
fun CustomCircleWithItem(
    center: LatLng,
    radius: Float,
    dragRadius: Float = radius,
    isDraggable: Boolean,
    color: String,
    borderColor: String = "Black",
    borderWidth: Float = 0.0f,
    opacity: Float = 1.0f,
    zIndex: Int = 0,
    imageId: Int? = null,
    itemSize: Float = 0.0f,
    text: String? = null,
    onCenterChanged: (LatLng) -> Unit = {},
    onDragStopped: () -> Unit = {},
    textColor: String = "Black"
) {
    val draggableCenterState = remember { mutableStateOf(center) }

    UpdateCenter(coord = center, centerUpdated = { draggableCenterState.value = it })

    // Invisible circle, this is the draggable
    Circle(
        center = draggableCenterState.value,
        radius = dragRadius,
        isDraggable = isDraggable,
        color = "Transparent",
        borderColor = borderColor,
        borderWidth = 0.0f,
        zIndex = zIndex + 1,
        onCenterDragged = {
            onCenterChanged(it)
        },
        onDragFinished = {
            draggableCenterState.value = center
            onDragStopped()
        },
    )

    // Display circle
    Circle(
        center = center,
        radius = radius,
        isDraggable = false,
        color = color,
        opacity = opacity,
        zIndex = zIndex,
        borderColor = borderColor,
        borderWidth = borderWidth,
        onCenterDragged = {}
    )

    imageId?.let {
        TextSymbol(
            center = center,
            color = "Black",
            isDraggable = false,
            imageId = imageId,
            size = itemSize,
            zIndex = zIndex + 1,
        )
    }

    text?.let {
        TextSymbol(
            center = center,
            color = textColor,
            isDraggable = false,
            text = text,
            size = itemSize,
            zIndex = zIndex + 1,
            imageId = null
        )
    }
}
