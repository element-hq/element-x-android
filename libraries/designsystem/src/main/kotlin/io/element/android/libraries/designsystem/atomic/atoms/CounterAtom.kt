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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

private const val MAX_COUNT = 99
private const val MAX_COUNT_STRING = "$MAX_COUNT+"

/**
 * A counter atom that displays a number in a circle.
 * Figma link : https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2805-2649&m=dev
 *
 * @param count The number to display. If the number is greater than [MAX_COUNT], the counter will display [MAX_COUNT_STRING].
 * If the number is less than 1, just a dot will be displayed instead.
 * @param modifier The modifier to apply to this layout.
 * @param containerColor The background color of the counter. When null, uses [isCritical] to pick a default.
 * @param contentColor The text color inside the counter. When null, uses [SemanticColors.textOnSolidPrimary].
 * @param contentPadding The padding to apply to the text inside the counter.
 * @param textStyle The style to apply to the text inside the counter.
 * @param dotSize The size of the dot when [count] is less than 1. It defaults to `12.0.dp`.
 * @param isCritical If true, the counter will use a critical color scheme, otherwise it will use an accent color scheme.
 * Only used when [containerColor] is null.
 */
@Composable
fun CounterAtom(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 0.5.dp),
    textStyle: TextStyle = CounterAtomDefaults.textStyle,
    dotSize: Dp = 12.0.dp,
    isCritical: Boolean = false,
) {
    val countAsText = when (count) {
        in Int.MIN_VALUE..0 -> null
        in 1..MAX_COUNT -> count.toString()
        else -> MAX_COUNT_STRING
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val minTextSize = remember(density.density, textStyle) {
        val result = textMeasurer.measure(
            text = MAX_COUNT_STRING,
            style = textStyle
        )
        with(density) {
            minOf(result.size.width, result.size.height).toDp()
        }
    }
    val sizeModifier = if (countAsText?.length == 1) {
        Modifier.size(minTextSize)
    } else {
        Modifier.sizeIn(minHeight = minTextSize)
    }

    val extraPadding = if (countAsText?.length == 1) {
        contentPadding
    } else {
        contentPadding + PaddingValues(horizontal = 4.dp)
    }
    val backgroundColor = containerColor ?: if (isCritical) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconAccentPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(backgroundColor)
            .then(
                if (countAsText == null) Modifier.size(dotSize) else Modifier
            )
    ) {
        countAsText?.let {
            Text(
                modifier = sizeModifier.padding(extraPadding),
                text = countAsText,
                style = textStyle,
                color = contentColor ?: ElementTheme.colors.textOnSolidPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

object CounterAtomDefaults {
    val textStyle: TextStyle
        @Composable get() = ElementTheme.typography.fontBodyMdMedium
}

@PreviewsDayNight
@Composable
internal fun CounterAtomPreview() = ElementPreview {
    Row(horizontalArrangement = spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        CounterAtom(count = 0)
        CounterAtom(count = 4)
        CounterAtom(count = 10)
        CounterAtom(count = 100)
        CounterAtom(count = 4, isCritical = true)
    }
}
