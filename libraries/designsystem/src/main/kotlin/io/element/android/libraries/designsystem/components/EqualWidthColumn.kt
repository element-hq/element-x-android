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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Used to create a column where all children have the same width.
 * It will first measure all children, get the largest width and re-measure all children with this width as the minWidth.
 *
 * *Note*: If all children already have the same width, it skips the 2nd measuring and acts like a normal Column.
 */
@Composable
fun EqualWidthColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measurables = subcompose(0, content).map { it.measure(constraints) }
        val maxWidth = measurables.maxOf { it.width }
        val newConstraints = constraints.copy(minWidth = maxWidth)
        val newMeasurables = if (measurables.all { it.width == maxWidth }) {
            // Skip re-measuring if all children have the same width
            measurables
        } else {
            // Re-measure with the largest width as the minWidth to have all children constrained to the same width
            subcompose(1, content).map { it.measure(newConstraints) }
        }
        val totalHeight = (newMeasurables.sumOf { it.height } + spacing.toPx() * (newMeasurables.size - 1)).roundToInt()
        layout(maxWidth, totalHeight) {
            var yPosition = 0
            newMeasurables.forEach { measurable ->
                measurable.placeRelative(0, yPosition)
                yPosition += measurable.height + spacing.roundToPx()
            }
        }
    }
}
