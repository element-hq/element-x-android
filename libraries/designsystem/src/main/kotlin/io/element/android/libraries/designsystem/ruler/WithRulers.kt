/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
internal fun WithRulersPreview() = ElementPreview {
    WithRulers(xRulersOffset = 20.dp, yRulersOffset = 15.dp) {
        OutlinedButton(
            text = "A Button with rulers on it!",
            size = ButtonSize.Medium,
            onClick = {},
        )
    }
}
