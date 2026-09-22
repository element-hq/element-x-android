/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Text

private const val MAX_COUNT = 99
private const val MAX_COUNT_STRING = "$MAX_COUNT+"

/**
 * A counter atom that displays a number in a circle.
 * Figma link : https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2805-2649&m=dev
 *
 * @param count The number to display. If the number is greater than [MAX_COUNT], the counter will display [MAX_COUNT_STRING].
 * If the number is less than 1, the counter will not be displayed.
 * @param modifier The modifier to apply to this layout.
 * @param containerColor The background color of the counter. When null, uses [isCritical] to pick a default.
 * @param contentColor The text color inside the counter. When null, uses [SemanticColors.textOnSolidPrimary].
 * @param textStyle The style to apply to the text inside the counter.
 * @param isCritical If true, the counter will use a critical color scheme, otherwise it will use an accent color scheme.
 * Only used when [containerColor] is null.
 */
@Composable
fun CounterAtom(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    textStyle: TextStyle = CounterAtomDefaults.textStyle,
    isCritical: Boolean = false,
) {
    if (count < 1) return
    val countAsText = when (count) {
        in 0..MAX_COUNT -> count.toString()
        else -> MAX_COUNT_STRING
    }
    val textMeasurer = rememberTextMeasurer()
    // Measure the maximum count string size
    val textLayoutResult = textMeasurer.measure(
        text = MAX_COUNT_STRING,
        style = textStyle
    )
    val textSize = textLayoutResult.size
    val badgeSize = textSize.height
    Box(
        modifier = modifier
            .then(if (count > MAX_COUNT) {
                Modifier
            } else {
                // We add 1.dp to the minimum size to make sure the text does not look cramped or is clipped
                Modifier.size(badgeSize.toDp() + 1.dp)
            })
            // Equivalent to adding a circle shape if the aspect ratio is 1:1, but allows for a pill shape if the count is greater than MAX_COUNT
            .clip(RoundedCornerShape(percent = 50))
            .background(
                containerColor ?: if (isCritical) {
                    ElementTheme.colors.iconCriticalPrimary
                } else {
                    ElementTheme.colors.iconAccentPrimary
                }
            )
            .then(
        if (count > MAX_COUNT) {
                    // We add some padding to the badge when the count is greater than MAX_COUNT to make it look like the circle-shaped badges
                    Modifier.padding(horizontal = 2.dp, vertical = 0.5.dp)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = countAsText,
            style = textStyle,
            color = contentColor ?: ElementTheme.colors.textOnSolidPrimary,
        )
    }
}

object CounterAtomDefaults {
    val textStyle: TextStyle
        @Composable get() = ElementTheme.typography.fontBodySmMedium
            // We use a negative letter spacing to make the text fit better in the badge.
            .copy(letterSpacing = (-0.25).sp)
}

@PreviewsDayNight
@Composable
internal fun CounterAtomPreview() = ElementPreview {
    Row(horizontalArrangement = spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        CounterAtom(count = 0)
        CounterAtom(count = 4)
        CounterAtom(count = 99)
        CounterAtom(count = 100)
        CounterAtom(count = 4, isCritical = true)
    }
}
