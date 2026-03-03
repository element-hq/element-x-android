/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapProjected(
    target: Position,
    cameraState: CameraState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                cameraState.position
                val offset = cameraState.projection?.screenLocationFromPosition(target)
                if (offset != null) {
                    translationX = offset.x.toPx() - size.width / 2
                    translationY = offset.y.toPx() - size.height
                }
            }
    ) {
        content()
    }
}
