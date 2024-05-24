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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.toSecondaryEnabledColor

@Composable
fun PreferenceCheckbox(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
) {
    ListItem(
        modifier = modifier,
        leadingContent = if (iconResourceId != null || icon != null || showIconAreaIfNoIcon) {
            ListItemContent.Custom {
                PreferenceIcon(
                    icon = icon,
                    iconResourceId = iconResourceId,
                    enabled = enabled,
                    isVisible = showIconAreaIfNoIcon
                )
            }
        } else {
            null
        },
        headlineContent = {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = enabled.toEnabledColor(),
            )
        },
        supportingContent = {
            if (supportingText != null) {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = supportingText,
                    color = enabled.toSecondaryEnabledColor(),
                )
            }
        },
        trailingContent = ListItemContent.Checkbox(
            checked = isChecked,
            onChange = onCheckedChange,
            enabled = enabled,
        ),
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceCheckboxPreview() = ElementThemedPreview {
    Column {
        PreferenceCheckbox(
            title = "Checkbox",
            iconResourceId = CompoundDrawables.ic_compound_threads,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = CompoundDrawables.ic_compound_threads,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = null,
            showIconAreaIfNoIcon = true,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = null,
            showIconAreaIfNoIcon = false,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
    }
}
