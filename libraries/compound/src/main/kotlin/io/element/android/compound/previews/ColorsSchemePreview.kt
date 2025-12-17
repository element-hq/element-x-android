/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.previews

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun ColorsSchemePreview(
    backgroundColor: Color,
    foregroundColor: Color,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
) {
    val colors = persistentMapOf(
        "primary" to colorScheme.primary,
        "onPrimary" to colorScheme.onPrimary,
        "primaryContainer" to colorScheme.primaryContainer,
        "onPrimaryContainer" to colorScheme.onPrimaryContainer,
        "inversePrimary" to colorScheme.inversePrimary,
        "secondary" to colorScheme.secondary,
        "onSecondary" to colorScheme.onSecondary,
        "secondaryContainer" to colorScheme.secondaryContainer,
        "onSecondaryContainer" to colorScheme.onSecondaryContainer,
        "tertiary" to colorScheme.tertiary,
        "onTertiary" to colorScheme.onTertiary,
        "tertiaryContainer" to colorScheme.tertiaryContainer,
        "onTertiaryContainer" to colorScheme.onTertiaryContainer,
        "background" to colorScheme.background,
        "onBackground" to colorScheme.onBackground,
        "surface" to colorScheme.surface,
        "onSurface" to colorScheme.onSurface,
        "surfaceVariant" to colorScheme.surfaceVariant,
        "onSurfaceVariant" to colorScheme.onSurfaceVariant,
        "surfaceTint" to colorScheme.surfaceTint,
        "inverseSurface" to colorScheme.inverseSurface,
        "inverseOnSurface" to colorScheme.inverseOnSurface,
        "error" to colorScheme.error,
        "onError" to colorScheme.onError,
        "errorContainer" to colorScheme.errorContainer,
        "onErrorContainer" to colorScheme.onErrorContainer,
        "outline" to colorScheme.outline,
        "outlineVariant" to colorScheme.outlineVariant,
        "scrim" to colorScheme.scrim,
    )
    ColorListPreview(
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        colors = colors,
        modifier = modifier,
    )
}
