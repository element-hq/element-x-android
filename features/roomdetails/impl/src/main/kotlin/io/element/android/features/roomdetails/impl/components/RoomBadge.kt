/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
