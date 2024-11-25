/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    gapSize: Dp = 0.dp,
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
) {
    androidx.compose.material3.LinearProgressIndicator(
        modifier = modifier,
        progress = progress,
        gapSize = gapSize,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap,
        drawStopIndicator = {},
    )
}

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.linearColor,
    gapSize: Dp = 0.dp,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
) {
    if (LocalInspectionMode.current) {
        // Use a determinate progress indicator to improve the preview rendering
        androidx.compose.material3.LinearProgressIndicator(
            modifier = modifier,
            progress = { 0.75F },
            gapSize = gapSize,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            drawStopIndicator = {},
        )
    } else {
        androidx.compose.material3.LinearProgressIndicator(
            modifier = modifier,
            color = color,
            gapSize = gapSize,
            trackColor = trackColor,
            strokeCap = strokeCap,
        )
    }
}

@Preview(group = PreviewGroup.Progress)
@Composable
internal fun LinearProgressIndicatorPreview() = ElementThemedPreview(vertical = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Indeterminate progress
        LinearProgressIndicator()
        // Fixed progress
        LinearProgressIndicator(
            progress = { 0.90F }
        )
    }
}
