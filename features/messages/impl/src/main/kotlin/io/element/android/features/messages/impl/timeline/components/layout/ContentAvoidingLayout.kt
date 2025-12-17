/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.layout

import android.text.Layout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.wysiwyg.compose.EditorStyledText
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A layout with 2 children: the [content] and the [overlay].
 *
 * It will try to place the [overlay] on top of the [content] if possible, avoiding the area of it that is non-overlapping.
 * If the [overlay] can't be placed on top of the [content], it will be placed to the right of it, if it fits, otherwise, to its bottom in a new row.
 *
 * @param overlay The 'overlay' component of the layout, which will be positioned relative to the [content].
 * @param modifier The modifier for the layout.
 * @param spacing The spacing between the [content] and the [overlay]. Defaults to `0.dp`.
 * @param overlayOffset The offset of the [overlay] from the bottom right corner of the [content].
 * @param shrinkContent Whether the content should be shrunk to fit the available width or not. Defaults to `false`.
 * @param content The 'content' component of the layout.
 */
@Composable
fun ContentAvoidingLayout(
    overlay: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    overlayOffset: DpOffset = DpOffset.Zero,
    shrinkContent: Boolean = false,
    content: @Composable ContentAvoidingLayoutScope.() -> Unit,
) {
    val scope = remember { ContentAvoidingLayoutScopeInstance() }

    Layout(
        modifier = modifier,
        content = {
            scope.content()
            overlay()
        }
    ) { measurables, constraints ->

        // Measure the `overlay` view first, in case we need to shrink the `content`
        val overlayPlaceable = measurables.last().measure(Constraints(minWidth = 0, maxWidth = constraints.maxWidth))
        val contentConstraints = if (shrinkContent) {
            Constraints(minWidth = 0, maxWidth = constraints.maxWidth - overlayPlaceable.width)
        } else {
            Constraints(minWidth = 0, maxWidth = constraints.maxWidth)
        }
        val contentPlaceable = measurables.first().measure(contentConstraints)

        var layoutWidth = contentPlaceable.width
        var layoutHeight = contentPlaceable.height

        val data = scope.data.value

        // Free space = width of the whole component - width of its non overlapping contents
        val freeSpace = max(contentPlaceable.width - data.nonOverlappingContentWidth, 0)

        when {
            // When the content + the overlay don't fit in the available max width, we need to move the overlay to a new row
            !shrinkContent && data.nonOverlappingContentWidth + overlayPlaceable.width > constraints.maxWidth -> {
                layoutHeight += overlayPlaceable.height + overlayOffset.y.roundToPx()
            }
            // If the content is smaller than the available max width, we can move the overlay to the right of the content
            contentPlaceable.width < constraints.maxWidth -> {
                // If both the content and the overlay plus the padding can fit inside the current layoutWidth, there is no need to increase it
                if (freeSpace < overlayPlaceable.width + spacing.roundToPx()) {
                    // Otherwise, we need to increase it by the width of the overlay + some padding adjustments
                    val calculatedWidth = max(data.nonOverlappingContentWidth + overlayPlaceable.width + spacing.roundToPx(), contentPlaceable.width)
                    layoutWidth = min(calculatedWidth, constraints.maxWidth)
                }
            }
            else -> Unit
        }

        layoutWidth = max(layoutWidth, constraints.minWidth)
        layoutHeight = max(layoutHeight, constraints.minHeight)

        layout(layoutWidth, layoutHeight) {
            contentPlaceable.placeRelative(0, 0)
            overlayPlaceable.placeRelative(layoutWidth - overlayPlaceable.width, layoutHeight - overlayPlaceable.height + overlayOffset.y.roundToPx())
        }
    }
}

/**
 * Data class to hold the content layout data.
 * This is used to pass the data from the content to the [ContentAvoidingLayout].
 *
 * @param contentWidth The full width of the content in pixels.
 * @param contentHeight The full height of the content in pixels.
 * @param nonOverlappingContentWidth The width of the part of the content that can't overlap with the timestamp.
 * @param nonOverlappingContentHeight The height of the part of the content that can't overlap with the timestamp.
 */
data class ContentAvoidingLayoutData(
    val contentWidth: Int = 0,
    val contentHeight: Int = 0,
    val nonOverlappingContentWidth: Int = contentWidth,
    val nonOverlappingContentHeight: Int = contentHeight,
)

/**
 * A scope for the [ContentAvoidingLayout].
 */
interface ContentAvoidingLayoutScope {
    /**
     * It should be called when the content layout changes, so it can update the [ContentAvoidingLayoutData] and measure and layout the content properly.
     */
    fun onContentLayoutChange(data: ContentAvoidingLayoutData)
}

private class ContentAvoidingLayoutScopeInstance(
    val data: MutableState<ContentAvoidingLayoutData> = mutableStateOf(ContentAvoidingLayoutData()),
) : ContentAvoidingLayoutScope {
    override fun onContentLayoutChange(data: ContentAvoidingLayoutData) {
        this.data.value = data
    }
}

object ContentAvoidingLayout {
    /**
     * Measures the last line of a [TextLayoutResult] and calls [onContentLayoutChange] with the [ContentAvoidingLayoutData].
     *
     * This is supposed to be used in the `onTextLayout` parameter of a Text based component.
     */
    @Composable
    internal fun measureLastTextLine(
        onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
        extraWidth: Dp = 0.dp,
    ): ((TextLayoutResult) -> Unit) {
        val layoutDirection = LocalLayoutDirection.current
        val extraWidthPx = extraWidth.roundToPx()
        return { textLayout: TextLayoutResult ->
            // We need to add the external extra width so it's not taken into account as 'free space'
            val lastLineWidth = when (layoutDirection) {
                LayoutDirection.Ltr -> textLayout.getLineRight(textLayout.lineCount - 1).roundToInt()
                LayoutDirection.Rtl -> textLayout.getLineLeft(textLayout.lineCount - 1).roundToInt()
            }
            val lastLineHeight = textLayout.getLineBottom(textLayout.lineCount - 1).roundToInt()
            onContentLayoutChange(
                ContentAvoidingLayoutData(
                    contentWidth = textLayout.size.width + extraWidthPx,
                    contentHeight = textLayout.size.height,
                    nonOverlappingContentWidth = lastLineWidth + extraWidthPx,
                    nonOverlappingContentHeight = lastLineHeight,
                )
            )
        }
    }

    /**
     * Measures the last line of a [Layout] and calls [onContentLayoutChange] with the [ContentAvoidingLayoutData].
     *
     * This is supposed to be used in the `onTextLayout` parameter of an [EditorStyledText] component.
     */
    @Composable
    internal fun measureLegacyLastTextLine(
        onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
        extraWidth: Dp = 0.dp,
    ): ((Layout) -> Unit) {
        val extraWidthPx = extraWidth.roundToPx()
        return { textLayout: Layout ->
            // We need to add the external extra width so it's not taken into account as 'free space'
            val lastLineWidth = textLayout.getLineWidth(textLayout.lineCount - 1).roundToInt()
            val lastLineHeight = textLayout.getLineBottom(textLayout.lineCount - 1)
            onContentLayoutChange(
                ContentAvoidingLayoutData(
                    contentWidth = textLayout.width + extraWidthPx,
                    contentHeight = textLayout.height,
                    nonOverlappingContentWidth = lastLineWidth + extraWidthPx,
                    nonOverlappingContentHeight = lastLineHeight,
                )
            )
        }
    }
}
