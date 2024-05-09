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

package io.element.android.features.roomdetails.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.Badge
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.badgeNegativeBackgroundColor
import io.element.android.libraries.designsystem.theme.badgeNegativeContentColor
import io.element.android.libraries.designsystem.theme.badgeNeutralBackgroundColor
import io.element.android.libraries.designsystem.theme.badgeNeutralContentColor
import io.element.android.libraries.designsystem.theme.badgePositiveBackgroundColor
import io.element.android.libraries.designsystem.theme.badgePositiveContentColor

object RoomBadge {
    enum class Type {
        Positive,
        Neutral,
        Negative
    }

    @Composable fun View(
        text: String,
        icon: ImageVector,
        type: Type,
    ) {
        val backgroundColor = when (type) {
            Type.Positive -> ElementTheme.colors.badgePositiveBackgroundColor
            Type.Neutral -> ElementTheme.colors.badgeNeutralBackgroundColor
            Type.Negative -> ElementTheme.colors.badgeNegativeBackgroundColor
        }
        val textColor = when (type) {
            Type.Positive -> ElementTheme.colors.badgePositiveContentColor
            Type.Neutral -> ElementTheme.colors.badgeNeutralContentColor
            Type.Negative -> ElementTheme.colors.badgeNegativeContentColor
        }
        val iconColor = when (type) {
            Type.Positive -> ElementTheme.colors.iconSuccessPrimary
            Type.Neutral -> ElementTheme.colors.iconSecondary
            Type.Negative -> ElementTheme.colors.iconCriticalPrimary
        }
        Badge(
            text = text,
            icon = icon,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            textColor = textColor,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomBadgePositivePreview() {
    ElementPreview {
        RoomBadge.View(
            text = "Trusted",
            icon = CompoundIcons.Verified(),
            type = RoomBadge.Type.Positive,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomBadgeNeutralPreview() {
    ElementPreview {
        RoomBadge.View(
            text = "Public room",
            icon = CompoundIcons.Public(),
            type = RoomBadge.Type.Neutral,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomBadgeNegativePreview() {
    ElementPreview {
        RoomBadge.View(
            text = "Not trusted",
            icon = CompoundIcons.Error(),
            type = RoomBadge.Type.Negative,
        )
    }
}
