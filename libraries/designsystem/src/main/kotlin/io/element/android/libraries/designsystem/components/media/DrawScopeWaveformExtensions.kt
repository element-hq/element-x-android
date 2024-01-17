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

package io.element.android.libraries.designsystem.components.media

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.max

fun DrawScope.drawWaveform(
    waveformData: ImmutableList<Float>,
    canvasSizePx: Size,
    brush: Brush,
    minimumGraphAmplitude: Float = 2F,
    lineWidth: Dp = 2.dp,
    linePadding: Dp = 2.dp,
) {
    val centerY = canvasSizePx.height / 2
    val cornerRadius = lineWidth / 2
    waveformData.forEachIndexed { index, amplitude ->
        val drawingAmplitude = max(minimumGraphAmplitude, amplitude * (canvasSizePx.height - 2))
        drawRoundRect(
            brush = brush,
            topLeft = Offset(
                x = index * (linePadding + lineWidth).toPx(),
                y = centerY - drawingAmplitude / 2
            ),
            size = Size(
                width = lineWidth.toPx(),
                height = drawingAmplitude
            ),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
            style = Fill
        )
    }
}
