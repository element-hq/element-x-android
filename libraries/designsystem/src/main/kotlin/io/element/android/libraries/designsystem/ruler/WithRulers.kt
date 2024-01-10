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

package io.element.android.libraries.designsystem.ruler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton

/**
 * Debug tool to add a vertical and a horizontal ruler on top of the content.
 */
@Composable
fun WithRulers(
    modifier: Modifier = Modifier,
    xRulersOffset: Dp = 0.dp,
    yRulersOffset: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = {
            content()
            VerticalRuler()
            HorizontalRuler()
        },
        measurePolicy = { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            // Use layout size of the first item (the content)
            layout(
                width = placeables.first().width,
                height = placeables.first().height
            ) {
                placeables.forEachIndexed { index, placeable ->
                    if (index == 0) {
                        placeable.place(0, 0)
                    } else {
                        placeable.place(xRulersOffset.roundToPx(), yRulersOffset.roundToPx())
                    }
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun WithRulerPreview() = ElementPreview {
    WithRulers(xRulersOffset = 20.dp, yRulersOffset = 15.dp) {
        OutlinedButton(
            text = "A Button with rulers on it!",
            size = ButtonSize.Medium,
            onClick = {},
        )
    }
}
