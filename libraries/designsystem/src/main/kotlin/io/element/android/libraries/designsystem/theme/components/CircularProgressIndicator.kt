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

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
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
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier,
        progress = progress,
        color = color,
        strokeWidth = strokeWidth,
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
) {
    if (LocalInspectionMode.current) {
        // Use a determinate progress indicator to improve the preview rendering
        androidx.compose.material3.CircularProgressIndicator(
            modifier = modifier,
            progress = { 0.75F },
            color = color,
            strokeWidth = strokeWidth,
        )
    } else {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth,
        )
    }
}

@Preview(group = PreviewGroup.Progress)
@Composable
internal fun CircularProgressIndicatorPreview() = ElementThemedPreview(vertical = false) { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Indeterminate progress
        CircularProgressIndicator()
        // Fixed progress
        CircularProgressIndicator(
            progress = { 0.90F }
        )
    }
}
