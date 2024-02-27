/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import io.element.android.libraries.designsystem.text.toPx

/**
 * Draw a border on corners around the content.
 */
fun Modifier.cornerBorder(
    strokeWidth: Dp,
    color: Color,
    cornerSizeDp: Dp,
) = composed(
    factory = {
        val strokeWidthPx = strokeWidth.toPx()
        val cornerSize = cornerSizeDp.toPx()
        drawWithContent {
            drawContent()
            val width = size.width
            val height = size.height
            drawPath(
                path = Path().apply {
                    // Top left corner
                    moveTo(0f, cornerSize)
                    lineTo(0f, 0f)
                    lineTo(cornerSize, 0f)
                    // Top right corner
                    moveTo(width - cornerSize, 0f)
                    lineTo(width, 0f)
                    lineTo(width, cornerSize)
                    // Bottom right corner
                    moveTo(width, height - cornerSize)
                    lineTo(width, height)
                    lineTo(width - cornerSize, height)
                    // Bottom left corner
                    moveTo(cornerSize, height)
                    lineTo(0f, height)
                    lineTo(0f, height - cornerSize)
                },
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    pathEffect = PathEffect.cornerPathEffect(strokeWidthPx / 2),
                    cap = StrokeCap.Round,
                ),
            )
        }
    }
)
