/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import kotlin.math.max
import kotlin.math.min

/**
 * Makes the content square in size.
 *
 * This is achieved by cropping incoming max constraints to the largest possible square size
 * and measuring the content using resulting constraints.
 * Next the size of layout is decided based on largest dimension of the measured content.
 * Finally the content is placed inside the square layout according to specified [position].
 *
 * If no square exists that falls within the size range of the incoming constraints,
 * the content will be laid out as usual, as if the modifier was not applied.
 *
 * @param position The fraction of the content's position inside its square layout.
 * It determines the point on the axis that was extended to make a square.
 * Typically you'd want to use values between `0` and `1`, inclusive, where `0`
 * will place the content at the "start" of the square, `0.5` in the middle, and `1` at the "end".
 */
@Stable
fun Modifier.squareSize(
    position: Float = 0.5f,
): Modifier =
    this.then(
        when {
            position == 0.5f -> SquareSizeCenter
            else -> createSquareSizeModifier(position = position)
        }
    )

private val SquareSizeCenter = createSquareSizeModifier(position = 0.5f)

private class SquareSizeModifier(
    private val position: Float,
    inspectorInfo: InspectorInfo.() -> Unit,
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val maxSquare = min(constraints.maxWidth, constraints.maxHeight)
        val minSquare = max(constraints.minWidth, constraints.minHeight)
        val squareExists = minSquare <= maxSquare

        val resolvedConstraints = constraints
            .takeUnless { squareExists }
            ?: constraints.copy(maxWidth = maxSquare, maxHeight = maxSquare)

        val placeable = measurable.measure(resolvedConstraints)

        return if (squareExists) {
            val size = max(placeable.width, placeable.height)
            layout(size, size) {
                val x = ((size - placeable.width) * position).toInt()
                val y = ((size - placeable.height) * position).toInt()
                placeable.placeRelative(x, y)
            }
        } else {
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (other !is SquareSizeModifier) return false

        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}

@Suppress("ModifierFactoryExtensionFunction", "ModifierFactoryReturnType")
private fun createSquareSizeModifier(
    position: Float,
) =
    SquareSizeModifier(
        position = position,
        inspectorInfo = debugInspectorInfo {
            name = "squareSize"
            properties["position"] = position
        },
    )

@Preview
@Composable
internal fun SquareSizeModifierLargeWidthPreview() {
    ElementPreview {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .background(Color.Gray)
                .squareSize(position = 0.25f)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .size(100.dp, 10.dp)
            )
        }
    }
}

@Preview
@Composable
internal fun SquareSizeModifierLargeHeightPreview() {
    ElementPreview {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .background(Color.Gray)
                .squareSize(position = 0.75f)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .size(10.dp, 100.dp)
            )
        }
    }
}

@Preview
@Composable
internal fun SquareSizeModifierInsideSquarePreview() {
    ElementPreview {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(120.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .width(100.dp)
                    .squareSize(position = 0.75f)
            )
        }
    }
}
