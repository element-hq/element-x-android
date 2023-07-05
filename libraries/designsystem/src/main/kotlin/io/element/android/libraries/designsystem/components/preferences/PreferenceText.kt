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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

/**
 * Tried to use ListItem, but it cannot really match the design. Keep custom Layout for now.
 */
@Composable
fun PreferenceText(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    currentValue: String? = null,
    loadingCurrentValue: Boolean = false,
    icon: ImageVector? = null,
    showIconAreaIfNoIcon: Boolean = false,
    tintColor: Color? = null,
    onClick: () -> Unit = {},
) {
    val minHeight = if (subtitle == null) preferenceMinHeightOnlyTitle else preferenceMinHeight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .clickable { onClick() }
            .padding(horizontal = preferencePaddingHorizontal, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreferenceIcon(
            icon = icon,
            isVisible = showIconAreaIfNoIcon,
            tintColor = tintColor ?: ElementTheme.materialColors.secondary
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = tintColor ?: ElementTheme.materialColors.primary,
            )
            if (subtitle != null) {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = subtitle,
                    color = tintColor ?: ElementTheme.materialColors.secondary,
                )
            }
        }
        if (currentValue != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp, end = 8.dp),
                text = currentValue,
                style = ElementTheme.typography.fontBodyXsMedium,
                color = ElementTheme.materialColors.secondary,
            )
        } else if (loadingCurrentValue) {
            CircularProgressIndicator(
                modifier = Modifier
                    .progressSemantics()
                    .padding(start = 16.dp, end = 8.dp)
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
                strokeWidth = 2.dp
            )
        }
    }
}

@ElementPreviews(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceTextPreview() {
    ElementPreview { ContentToPreview() }
}

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
        PreferenceText(
            title = "Title no icon with icon area",
            showIconAreaIfNoIcon = true,
            loadingCurrentValue = true,
        )
        PreferenceText(
            title = "Title no icon",
            loadingCurrentValue = true,
        )
    }
}
