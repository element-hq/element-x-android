/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun CircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    trackColor: Color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        progress = progress,
        color = color,
        trackColor = trackColor,
        strokeWidth = strokeWidth,
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
) {
    if (LocalInspectionMode.current) {
        // Use a determinate progress indicator to improve the preview rendering
        androidx.compose.material3.CircularProgressIndicator(
            modifier = modifier,
            progress = { 0.75F },
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
        )
    } else {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
        )
    }
}

@Preview(group = PreviewGroup.Progress)
@Composable
internal fun CircularProgressIndicatorPreview() = ElementThemedPreview(vertical = false) {
    Column(
        modifier = Modifier.padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indeterminate progress
        Text("Indeterminate")
        CircularProgressIndicator()
        // Fixed progress
        Text("Fixed progress")
        CircularProgressIndicator(
            progress = { 0.50F }
        )
    }
}
