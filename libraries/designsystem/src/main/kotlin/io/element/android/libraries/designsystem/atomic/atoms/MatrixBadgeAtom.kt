/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.Badge
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

object MatrixBadgeAtom {
    data class MatrixBadgeData(
        val text: String,
        val icon: ImageVector,
        val type: Type,
    )

    enum class Type {
        Positive,
        Neutral,
        Negative,
        Info,
    }

    @Composable
    fun View(
        data: MatrixBadgeData,
    ) {
        val backgroundColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.bgBadgeAccent
            Type.Neutral -> ElementTheme.colors.bgBadgeDefault
            Type.Negative -> ElementTheme.colors.bgCriticalSubtle
            Type.Info -> ElementTheme.colors.bgBadgeInfo
        }
        val textColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.textBadgeAccent
            Type.Neutral -> ElementTheme.colors.textPrimary
            Type.Negative -> ElementTheme.colors.textCriticalPrimary
            Type.Info -> ElementTheme.colors.textBadgeInfo
        }
        val iconColor = when (data.type) {
            Type.Positive -> ElementTheme.colors.iconAccentPrimary
            Type.Neutral -> ElementTheme.colors.iconPrimary
            Type.Negative -> ElementTheme.colors.iconCriticalPrimary
            Type.Info -> ElementTheme.colors.iconInfoPrimary
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
            icon = CompoundIcons.ErrorSolid(),
            type = MatrixBadgeAtom.Type.Negative,
        )
    )
}

@PreviewsDayNight
@Composable
internal fun MatrixBadgeAtomInfoPreview() = ElementPreview {
    MatrixBadgeAtom.View(
        MatrixBadgeAtom.MatrixBadgeData(
            text = "Not encrypted",
            icon = CompoundIcons.LockOff(),
            type = MatrixBadgeAtom.Type.Info,
        )
    )
}
