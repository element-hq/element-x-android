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

package io.element.android.features.messages.impl.timeline.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A layout with 2 children: the `content` and the `overlay`.
 *
 * It will try to place the `overlay` on top of the `content` if possible, avoiding the area of it that is non-overlapping.
 * If the `overlay` can't be placed on top of the `content`, it will be placed to the right of it, if it fits, otherwise, to its bottom in a new row.
 *
 * @param modifier The modifier for the layout.
 * @param spacing The spacing between the `content` and the `overlay`. Defaults to `0.dp`.
 * @param shrinkContent Whether the content should be shrunk to fit the available width or not. Defaults to `false`.
 * @param layoutContent The content of the layout. **It must have exactly 2 children.**
 */
@Composable
fun ContentAvoidingLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    shrinkContent: Boolean = false,
    layoutContent: @Composable ContentAvoidingLayoutScope.() -> Unit,
) {
    val scope = remember { ContentAvoidingLayoutScopeInstance() }

    Layout(modifier = modifier, content = { scope.layoutContent() }) { measurables, constraints ->
        assert(measurables.size == 2) { "ContentAvoidingLayout must have exactly 2 children" }

        // Measure the `overlay` view first, in case we need to shrink the `content`
        val overlay = measurables.last().measure(Constraints(minWidth = 0, maxWidth = constraints.maxWidth))
        val contentConstraints = if (shrinkContent) {
            Constraints(minWidth = 0, maxWidth = constraints.maxWidth - overlay.width)
        } else {
            Constraints(minWidth = 0, maxWidth = constraints.maxWidth)
        }
        val content = measurables.first().measure(contentConstraints)

        var layoutWidth = content.width
        var layoutHeight = content.height

        val data = scope.data

        // Horizontal padding = width of the component - width of its contents, but only if `hasPadding` is true
        val internalContentHorizontalPadding = if (data.hasPadding) {
            content.width - data.contentWidth
        } else {
            0
        }

        when {
            // When the content + the overlay don't fit in the available max width, we need to move the overlay to a new row
            !shrinkContent && data.nonOverlappingContentWidth + overlay.width > constraints.maxWidth -> {
                layoutHeight += overlay.height
            }
            // If the content is smaller than the available max width, we can move the overlaidView to the right of the content
            layoutWidth < constraints.maxWidth -> {
                // If both the content and the overlay plus the padding can fit inside the current layoutWidth, there is no need to increase it
                if (data.nonOverlappingContentWidth + internalContentHorizontalPadding + overlay.width > layoutWidth) {
                    // Otherwise, we need to increase it by the width of the overlay + some padding adjustments
                    layoutWidth += overlay.width + spacing.roundToPx() - internalContentHorizontalPadding / 2
                }
            }
            else -> Unit
        }

        layoutWidth = max(layoutWidth, constraints.minWidth)
        layoutHeight = max(layoutHeight, constraints.minHeight)

        layout(layoutWidth, layoutHeight) {
            content.placeRelative(0, 0)
            overlay.placeRelative(layoutWidth - overlay.width, layoutHeight - overlay.height)
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
 * @param hasPadding Whether the content has padding or not. Defaults to `false`.
 */
@Suppress("DataClassShouldBeImmutable")
data class ContentAvoidingLayoutData(
    var contentWidth: Int = 0,
    var contentHeight: Int = 0,
    var nonOverlappingContentWidth: Int = contentWidth,
    var nonOverlappingContentHeight: Int = contentHeight,
    var hasPadding: Boolean = false,
)

/**
 * A scope for the [ContentAvoidingLayout].
 */
interface ContentAvoidingLayoutScope {

    /**
     * It should be called when the content layout changes, so it can update the [ContentAvoidingLayoutData] and measure and layout the content properly.
     */
    fun onContentLayoutChanged(data: ContentAvoidingLayoutData)
}

private class ContentAvoidingLayoutScopeInstance(
    val data: ContentAvoidingLayoutData = ContentAvoidingLayoutData(),
) : ContentAvoidingLayoutScope {
    override fun onContentLayoutChanged(data: ContentAvoidingLayoutData) {
        this.data.contentWidth = data.contentWidth
        this.data.contentHeight = data.contentHeight
        this.data.nonOverlappingContentWidth = data.nonOverlappingContentWidth
        this.data.nonOverlappingContentHeight = data.nonOverlappingContentHeight
        this.data.hasPadding = data.hasPadding
    }
}
