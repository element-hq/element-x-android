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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    minZoom: Float = 1f,
    maxZoom: Float = 5f,
    content: @Composable ZoomableBoxScope.() -> Unit
) {
    var zoom by remember { mutableStateOf(minZoom) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged {
                size = it
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    zoom = (zoom * zoomChange).coerceIn(minZoom, maxZoom)
                    val maxX = (size.width * (zoom - 1)) / 2f
                    val minX = -maxX
                    val maxY = (size.height * (zoom - 1)) / 2f
                    val minY = -maxY
                    offsetX = maxOf(minX, minOf(maxX, offsetX + panChange.x))
                    offsetY = maxOf(minY, minOf(maxY, offsetY + panChange.y))
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        offsetX = 0f
                        offsetY = 0f
                        zoom = if (zoom > minZoom) {
                            minZoom
                        } else {
                            maxZoom / 2f
                        }

                    }
                )
            },
        contentAlignment = contentAlignment,
    ) {
        DefaultZoomableBoxScope(this, zoom, offsetX, offsetY).content()
    }
}

@LayoutScopeMarker
@Immutable
interface ZoomableBoxScope : BoxScope {
    @Stable
    fun Modifier.zoomable(): Modifier
}

private class DefaultZoomableBoxScope(
    private val parentScope: BoxScope,
    private val scale: Float,
    private val offsetX: Float,
    private val offsetY: Float
) : ZoomableBoxScope, BoxScope by parentScope {

    override fun Modifier.zoomable() = this.then(
        graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offsetX,
            translationY = offsetY,
        )
    )
}
