/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import kotlin.math.max

/**
 * A layout with a [header], a [footer] and a [content] composables.
 * The component can be scrollable or not, which will modify which layout is used internally to arrange the UI.
 * In both cases, the [content] will use all the available space between the [header] and the [footer].
 * The [footer] is always visible, the [header] and the [content] can be scrolled if [isScrollable] is true.
 *
 * @param header the header composable.
 * @param footer the footer composable.
 * @param isScrollable if the content should be scrollable.
 * @param contentInsetsPadding padding values to apply to the content. This is useful to handle system insets.
 * @param modifier Compose modifier.
 * @param content the main content composable.
 */
@Suppress("ContentSlotReused")
@Composable
fun HeaderFooterLayout(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    isScrollable: Boolean,
    contentInsetsPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        if (isScrollable) {
            // This is a hack to make the content at least 1px high, otherwise the layout will crash
            var newHeight by remember { mutableIntStateOf(1) }
            Box(
                modifier = Modifier
                    .padding(contentInsetsPadding)
                    .weight(1f, fill = true)
                    .onSizeChanged {
                        newHeight = it.height
                    }
            ) {
                Layout(
                    content = {
                        header()
                        content()
                    },
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    measurePolicy = { measurables, constraints ->
                        val actualConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = newHeight)
                        val headerPlaceable = measurables.firstOrNull()?.measure(actualConstraints)

                        val contentPlaceable = if (headerPlaceable != null && measurables.size > 1) {
                            val availableContentHeight = max(1, actualConstraints.maxHeight - headerPlaceable.height)
                            val contentConstraints = actualConstraints.copy(minHeight = availableContentHeight, maxHeight = Constraints.Infinity)
                            measurables[1].measure(contentConstraints)
                        } else {
                            null
                        }

                        val headerHeight = headerPlaceable?.height ?: 0
                        val contentHeight = contentPlaceable?.height ?: 0
                        layout(actualConstraints.maxWidth, headerHeight + contentHeight) {
                            var yPosition = 0

                            headerPlaceable?.let {
                                it.placeRelative(0, yPosition)
                                yPosition += it.height
                            }

                            contentPlaceable?.placeRelative(0, yPosition)
                        }
                    }
                )
            }
        } else {
            Box(Modifier.padding(contentInsetsPadding)) {
                header()
            }
            Box(Modifier.weight(1f, fill = true)) {
                content()
            }
        }

        footer()
    }
}
