/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

/**
 * Compact selection indicator used in the timeline selection mode, in place of a Material
 * [androidx.compose.material3.Checkbox] (which reserves a 48dp interactive area). It is purely
 * visual: the tap is handled by the whole-row click target, so this only reflects [selected].
 */
@Composable
internal fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        imageVector = if (selected) CompoundIcons.CheckCircleSolid() else CompoundIcons.Circle(),
        contentDescription = null,
        tint = if (selected) ElementTheme.colors.iconAccentPrimary else ElementTheme.colors.iconTertiary,
    )
}

@PreviewsDayNight
@Composable
internal fun SelectionIndicatorPreview() = ElementPreview {
    Icon(
        modifier = Modifier.size(24.dp),
        imageVector = CompoundIcons.Circle(),
        contentDescription = null,
        tint = ElementTheme.colors.iconTertiary,
    )
}

@PreviewsDayNight
@Composable
internal fun SelectionIndicatorSelectedPreview() = ElementPreview {
    SelectionIndicator(selected = true)
}
