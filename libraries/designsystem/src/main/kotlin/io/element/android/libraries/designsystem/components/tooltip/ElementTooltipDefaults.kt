/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.tooltip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

object ElementTooltipDefaults {
    /**
     * Creates a [PopupPositionProvider] that allows adding padding between the edge of the
     * window and the tooltip.
     *
     * It is a wrapper around [TooltipDefaults.rememberPlainTooltipPositionProvider] and is
     * designed for use with a [PlainTooltip].
     *
     * @param spacingBetweenTooltipAndAnchor the spacing between the tooltip and the anchor.
     * @param windowPadding the padding between the tooltip and the edge of the window.
     *
     * @return a [PopupPositionProvider].
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun rememberPlainTooltipPositionProvider(
        spacingBetweenTooltipAndAnchor: Dp = 8.dp,
        windowPadding: Dp = 12.dp,
    ): PopupPositionProvider {
        val windowPaddingPx = with(LocalDensity.current) { windowPadding.roundToPx() }
        val plainTooltipPositionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = spacingBetweenTooltipAndAnchor,
        )
        return remember(windowPaddingPx, plainTooltipPositionProvider) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = plainTooltipPositionProvider
                    .calculatePosition(
                        anchorBounds = anchorBounds,
                        windowSize = windowSize,
                        layoutDirection = layoutDirection,
                        popupContentSize = popupContentSize
                    )
                    .let {
                        val maxX = windowSize.width - popupContentSize.width - windowPaddingPx
                        val maxY = windowSize.height - popupContentSize.height - windowPaddingPx
                        if (maxX <= windowPaddingPx || maxY <= windowPaddingPx) {
                            return@let it
                        }
                        IntOffset(
                            x = it.x.coerceIn(
                                minimumValue = windowPaddingPx,
                                maximumValue = maxX,
                            ),
                            y = it.y.coerceIn(
                                minimumValue = windowPaddingPx,
                                maximumValue = maxY,
                            )
                        )
                    }
            }
        }
    }
}
