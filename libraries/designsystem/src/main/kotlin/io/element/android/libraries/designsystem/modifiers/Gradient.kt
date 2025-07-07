/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.LocalBuildMeta

/**
 * Ref: https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Workspaces-V1?node-id=1141-24692
 */
@Stable
@Composable
fun Modifier.backgroundVerticalGradient(
    isVisible: Boolean = true,
    isEnterpriseBuild: Boolean = LocalBuildMeta.current.isEnterpriseBuild,
): Modifier {
    if (!isVisible) return this
    return background(
        brush = Brush.verticalGradient(
            colorStops = subtleColorStops(isEnterpriseBuild),
        ),
        alpha = 0.75f,
    )
}

@Composable
fun subtleColorStops(
    isEnterpriseBuild: Boolean = LocalBuildMeta.current.isEnterpriseBuild,
): Array<Pair<Float, Color>> {
    return buildList {
        if (isEnterpriseBuild) {
            // For enterprise builds, ensure that we are theming the gradient
            add(0f to ElementTheme.colors.textActionAccent.copy(alpha = 0.5f))
            add(0.75f to ElementTheme.colors.bgCanvasDefault)
            add(1f to Color.Transparent)
        } else {
            add(0f to ElementTheme.colors.gradientSubtleStop1)
            add(1 / 5f to ElementTheme.colors.gradientSubtleStop2)
            add(2 / 5f to ElementTheme.colors.gradientSubtleStop3)
            add(3 / 5f to ElementTheme.colors.gradientSubtleStop4)
            add(4 / 5f to ElementTheme.colors.gradientSubtleStop5)
            add(1f to ElementTheme.colors.gradientSubtleStop6)
        }
    }.toTypedArray()
}

@PreviewsDayNight
@Composable
internal fun BackgroundVerticalGradientPreview() = ElementPreview {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 100.dp)
            .backgroundVerticalGradient()
    )
}

@PreviewsDayNight
@Composable
internal fun BackgroundVerticalGradientEnterprisePreview() = ElementPreview {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 100.dp)
            .backgroundVerticalGradient(
                isEnterpriseBuild = true,
            )
    )
}

@PreviewsDayNight
@Composable
internal fun BackgroundVerticalGradientDisabledPreview() = ElementPreview {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 100.dp)
            .backgroundVerticalGradient(
                isVisible = false,
            )
    )
}
