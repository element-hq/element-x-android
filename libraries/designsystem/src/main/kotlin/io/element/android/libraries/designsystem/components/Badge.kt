/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.badgePositiveBackgroundColor
import io.element.android.libraries.designsystem.theme.badgePositiveContentColor
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun Badge(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color,
    iconColor: Color,
    shape: Shape = RoundedCornerShape(50),
    borderStroke: BorderStroke? = null,
    tintIcon: Boolean = true,
) {
    Surface(
        color = backgroundColor,
        contentColor = textColor,
        border = borderStroke,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 4.5.dp, bottom = 4.5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (tintIcon) iconColor else LocalContentColor.current,
            )
            Text(
                text = text,
                style = ElementTheme.typography.fontBodySmRegular,
                color = textColor,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BadgePreview() {
    ElementPreview {
        Badge(
            text = "Trusted",
            icon = CompoundIcons.Verified(),
            backgroundColor = ElementTheme.colors.badgePositiveBackgroundColor,
            textColor = ElementTheme.colors.badgePositiveContentColor,
            iconColor = ElementTheme.colors.iconSuccessPrimary,
        )
    }
}
