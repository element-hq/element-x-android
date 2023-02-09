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

package io.element.android.libraries.designsystem.components.preferences

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewDefaults
import io.element.android.libraries.designsystem.theme.components.Slider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor

@Composable
fun PreferenceSlide(
    title: String,
    @FloatRange(0.0, 1.0)
    value: Float,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    summary: String? = null,
    steps: Int = 0,
    onValueChange: (Float) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = preferenceMinHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PreferenceIcon(icon = icon)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = preferencePaddingEnd),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = enabled.toEnabledColor(),
                    text = title
                )
                summary?.let {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = enabled.toEnabledColor(),
                        text = summary
                    )
                }
                Slider(
                    value = value,
                    steps = steps,
                    onValueChange = onValueChange,
                    enabled = enabled,
                )
            }
        }
    }
}

@PreviewDefaults
@Composable
internal fun PreferenceSlidePreview() = ElementPreview {
    PreferenceSlide(
        title = "Slide",
        summary = "Summary",
        value = 0.75F
    )
}
