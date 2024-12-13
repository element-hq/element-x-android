/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.CircleOptions

@Composable
@MapLibreMapComposable
public fun Circle(
    center: LatLng,
    radius: Float,
    isDraggable: Boolean = false,
    color: String = "Yellow",
    opacity: Float = 1.0f,
    borderColor: String = "Black",
    borderWidth: Float = 0.0F,
    zIndex: Int = 0,
    onCenterDragged: (LatLng) -> Unit = {},
    onDragFinished: (LatLng) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as MapApplier

    ComposeNode<MapApplier.CircleNode, MapApplier>(factory = {
        val circleManager = mapApplier.getOrCreateCircleManagerForZIndex(zIndex)

        val circleOptions = CircleOptions()
            .withCircleRadius(radius)
            .withLatLng(center)
            .withDraggable(isDraggable)
            .withCircleStrokeColor(borderColor)
            .withCircleStrokeWidth(borderWidth)
            .withCircleOpacity(opacity)

        val circle = circleManager.create(circleOptions)

        MapApplier.CircleNode(
            circleManager,
            circle,
            onCircleDragged = { onCenterDragged(it.latLng) },
            onCircleDragStopped = { onDragFinished(it.latLng) },
        )
    }, update = {
        update(onCenterDragged) {
            this.onCircleDragged = { onCenterDragged(it.latLng) }
        }

        update(onDragFinished) {
            this.onCircleDragStopped = { onDragFinished(it.latLng) }
        }

        set(center) {
            circle.latLng = center
            circleManager.update(circle)
        }

        set(color) {
            circle.circleColor = color
            circleManager.update(circle)
        }

        set(radius) {
            circle.circleRadius = radius
            circleManager.update(circle)
        }
    })
}
