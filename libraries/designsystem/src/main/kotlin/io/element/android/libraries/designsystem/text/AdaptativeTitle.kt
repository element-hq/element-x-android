/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Render the title of a screen, but adapt the style if it does not fit on a single line,
 * so that a wrapped title does not look oversized.
 *
 * @param title the title to render.
 * @param style the style to use to render the [title].
 * @param twoLinesStyle the style to use instead of [style] when the [title] does not fit on a single line with [style],
 * so that a wrapped title does not look oversized.
 * @param modifier the [Modifier] to apply to this layout.
 */
@Composable
fun AdaptativeTitle(
    title: String,
    style: TextStyle,
    twoLinesStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    // If style and twoLinesStyle are the same, we don't need to measure the text.
    if (style == twoLinesStyle) {
        Text(
            modifier = modifier.semantics {
                heading()
            },
            style = style,
            maxLines = 2,
            text = title,
        )
    } else {
        val textMeasurer = rememberTextMeasurer()
        BoxWithConstraints(modifier = modifier) {
            // Measure the title before rendering it, so that the style is picked in a single layout pass.
            val fitsOnASingleLine = textMeasurer.measure(
                text = title,
                style = style,
                constraints = constraints,
            ).lineCount == 1
            Text(
                modifier = Modifier.semantics {
                    heading()
                },
                style = if (fitsOnASingleLine) style else twoLinesStyle,
                maxLines = 2,
                text = title,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AdaptativeTitlePreview() = ElementPreview {
    @Composable
    fun AdaptativeTitleItem(
        text: String,
        content: @Composable () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .background(ElementTheme.colors.bgSubtlePrimary),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = text,
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
            )
            content()
        }
    }
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Fits on a single line: the large style is kept.
        AdaptativeTitleItem("Adaptative, 1 line:") {
            AdaptativeTitle(
                title = "Chats",
                style = ElementTheme.typography.fontHeadingLgBold,
                twoLinesStyle = ElementTheme.typography.fontHeadingSmMedium,
            )
        }
        // Would be rendered on two lines with the large style: the smaller style is used instead.
        AdaptativeTitleItem("Adaptative, 2 lines:") {
            AdaptativeTitle(
                title = "Tutte le conversazioni:",
                style = ElementTheme.typography.fontHeadingLgBold,
                twoLinesStyle = ElementTheme.typography.fontHeadingSmMedium,
            )
        }
        // Would be rendered on two lines with the large style: the smaller style is used instead
        // and it's finally rendered on a single line with the smaller style.
        AdaptativeTitleItem("Adaptative, but 1 line:") {
            AdaptativeTitle(
                title = "Tous les chats",
                style = ElementTheme.typography.fontHeadingLgBold,
                twoLinesStyle = ElementTheme.typography.fontHeadingSmMedium,
            )
        }
        // Short title, with small font, 1 line.
        AdaptativeTitleItem("Always small, 1 line:") {
            AdaptativeTitle(
                title = "My space",
                style = ElementTheme.typography.fontHeadingSmMedium,
                twoLinesStyle = ElementTheme.typography.fontHeadingSmMedium,
            )
        }
        // Space name, which can still be rendered on two lines.
        AdaptativeTitleItem("Always small, 2 lines:") {
            AdaptativeTitle(
                title = "A very long space name",
                style = ElementTheme.typography.fontHeadingSmMedium,
                twoLinesStyle = ElementTheme.typography.fontHeadingSmMedium,
            )
        }
    }
}
