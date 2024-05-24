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
import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.components.PreferenceIcon
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Slider
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun PreferenceSlide(
    title: String,
    @FloatRange(0.0, 1.0)
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
    enabled: Boolean = true,
    summary: String? = null,
    steps: Int = 0,
) {
    ListItem(
        modifier = modifier,
        enabled = enabled,
        leadingContent = if (iconResourceId != null || icon != null || showIconAreaIfNoIcon) {
            ListItemContent.Custom {
                PreferenceIcon(
                    icon = icon,
                    iconResourceId = iconResourceId,
                    isVisible = showIconAreaIfNoIcon,
                )
            }
        } else {
            null
        },
        headlineContent = {
            Column {
                Text(
                    style = ElementTheme.typography.fontBodyLgRegular,
                    text = title,
                )
                summary?.let {
                    Text(
                        style = ElementTheme.typography.fontBodyMdRegular,
                        text = summary,
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
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceSlidePreview() = ElementThemedPreview {
    PreferenceSlide(
        icon = CompoundIcons.UserProfile(),
        title = "Slide",
        summary = "Summary",
        enabled = false,
        value = 0.75F,
        onValueChange = {},
    )
}
