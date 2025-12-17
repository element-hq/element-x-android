/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.colors.gradientSubtleColors
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Ref: https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Workspaces-V1?node-id=1141-24692
 */
@Stable
@Composable
fun Modifier.backgroundVerticalGradient(
    isVisible: Boolean = true,
): Modifier {
    if (!isVisible) return this
    return background(
        brush = Brush.verticalGradient(
            colors = gradientSubtleColors(),
        ),
    )
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
