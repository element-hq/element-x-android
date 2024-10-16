/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

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

object MatrixBadgeAtom {
    data class MatrixBadgeData(
        val text: String,
        val icon: ImageVector,
        val type: Type,
    )

    enum class Type {
        Positive,
        Neutral,
        Negative
    }

    @Composable
    fun View(
        data: MatrixBadgeData,
    ) {
        val backgroundColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.badgePositiveBackgroundColor
            Type.Neutral -> ElementTheme.colors.badgeNeutralBackgroundColor
            Type.Negative -> ElementTheme.colors.badgeNegativeBackgroundColor
        }
        val textColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.badgePositiveContentColor
            Type.Neutral -> ElementTheme.colors.badgeNeutralContentColor
            Type.Negative -> ElementTheme.colors.badgeNegativeContentColor
        }
        val iconColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.iconSuccessPrimary
            Type.Neutral -> ElementTheme.colors.iconSecondary
            Type.Negative -> ElementTheme.colors.iconCriticalPrimary
        }
        Badge(
            text = data.text,
            icon = data.icon,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            textColor = textColor,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomPositivePreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Trusted",
            icon = CompoundIcons.Verified(),
            type = MatrixBadgeAtom.Type.Positive,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomNeutralPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Public room",
            icon = CompoundIcons.Public(),
            type = MatrixBadgeAtom.Type.Neutral,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomNegativePreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Not trusted",
            icon = CompoundIcons.Error(),
            type = MatrixBadgeAtom.Type.Negative,
        )
    )
}
