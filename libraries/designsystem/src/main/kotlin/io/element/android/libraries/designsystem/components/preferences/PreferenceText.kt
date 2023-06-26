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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun PreferenceText(
    title: String?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    currentValue: String? = null,
    loadingCurrentValue: Boolean = false,
    icon: ImageVector? = null,
    tintColor: Color? = null,
    onClick: () -> Unit = {},
) {
    val minHeight = if (subtitle == null) preferenceMinHeightOnlyTitle else preferenceMinHeight
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clickable { onClick() }
            .padding(end = preferencePaddingHorizontal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = preferencePaddingVertical)
        ) {
            PreferenceIcon(icon = icon, tintColor = tintColor)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                if (title != null) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = title,
                        color = tintColor ?: MaterialTheme.colorScheme.primary,
                    )
                }
                if (title != null && subtitle != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (subtitle != null) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = subtitle,
                        color = tintColor ?: MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            if (currentValue != null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 16.dp),
                    text = currentValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else if (loadingCurrentValue) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .padding(horizontal = 16.dp)
                        .size(20.dp)
                        .align(Alignment.CenterVertically),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextPreview() = ElementThemedPreview { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PreferenceText(
            title = "Title",
            icon = Icons.Default.BugReport,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = Icons.Default.BugReport,
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = Icons.Default.BugReport,
            currentValue = "123",
        )
        PreferenceText(
            title = "Title",
            subtitle = "Some content",
            icon = Icons.Default.BugReport,
            loadingCurrentValue = true,
        )
        PreferenceText(
            title = "Title",
            icon = Icons.Default.BugReport,
            currentValue = "123",
        )
        PreferenceText(
            title = "Title",
            icon = Icons.Default.BugReport,
            loadingCurrentValue = true,
        )
    }
}
