/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
@Suppress("ModifierComposed")
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
