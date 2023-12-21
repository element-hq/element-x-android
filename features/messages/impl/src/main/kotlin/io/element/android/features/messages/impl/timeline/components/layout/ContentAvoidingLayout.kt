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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.features.messages.impl.timeline.components.TimelineEventTimestampViewDefaults
import kotlin.math.max

@Composable
fun ContentAvoidingLayout(
    modifier: Modifier = Modifier,
    shrinkContent: Boolean = false,
    content: @Composable ContentAvoidingLayoutScope.() -> Unit,
) {
    val scope = remember { ContentAvoidingLayoutScopeInstance() }

    Layout(modifier = modifier, content = { scope.content() }) { measurables, constraints ->
        assert(measurables.size == 2) { "TextAvoidingLayout must have exactly 2 children" }

        val timestamp = measurables.last().measure(Constraints(minWidth = 0, maxWidth = constraints.maxWidth))
        val contentConstraints = if (shrinkContent) {
            Constraints(minWidth = 0, maxWidth =  constraints.maxWidth - timestamp.width)
        } else {
            Constraints(minWidth = 0, maxWidth = constraints.maxWidth)
        }
        val messageContent = measurables.first().measure(contentConstraints)

        var layoutWidth = messageContent.width
        var layoutHeight = messageContent.height

        val data = scope.data

        // Horizontal padding = width of the component - width of its contents
        val internalContentHorizontalPadding = if (data.hasPadding) {
            messageContent.width - data.contentWidth
        } else {
            0
        }

        when {
            !shrinkContent && data.nonOverlappingContentWidth + timestamp.width > constraints.maxWidth -> {
                layoutHeight += timestamp.height
            }
            layoutWidth < constraints.maxWidth -> {
                if (data.nonOverlappingContentWidth + internalContentHorizontalPadding + timestamp.width > layoutWidth) {
                    layoutWidth += timestamp.width - TimelineEventTimestampViewDefaults.padding.horizontalPadding().roundToPx() - internalContentHorizontalPadding / 2
                }
            }
            else -> Unit
        }

        layoutWidth = max(layoutWidth, constraints.minWidth)
        layoutHeight = max(layoutHeight, constraints.minHeight)

        layout(layoutWidth, layoutHeight) {
            messageContent.placeRelative(0, 0)
            timestamp.placeRelative(layoutWidth - timestamp.width, layoutHeight - timestamp.height)
        }
    }
}

data class ContentAvoidingLayoutData(
    var contentWidth: Int = 0,
    var contentHeight: Int = 0,
    var contentStart: Int = 0,
    var nonOverlappingContentWidth: Int = contentWidth,
    var nonOverlappingContentHeight: Int = contentHeight,
    var hasPadding: Boolean = false,
)

interface ContentAvoidingLayoutScope {
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
        this.data.contentStart = data.contentStart
        this.data.hasPadding = data.hasPadding
    }
}

private fun PaddingValues.horizontalPadding(): Dp {
    return this.calculateLeftPadding(LayoutDirection.Ltr) + this.calculateRightPadding(LayoutDirection.Ltr)
}
