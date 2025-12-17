/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.DarkColorTokens

/**
 * See the mapping in
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=311-14&p=f&t=QcVyNaPEZMDA6RFK-0
 */
@OptIn(CoreColorToken::class)
fun SemanticColors.toMaterialColorSchemeDark(): ColorScheme = darkColorScheme(
    primary = bgActionPrimaryRest,
    onPrimary = textOnSolidPrimary,
    primaryContainer = bgCanvasDefault,
    onPrimaryContainer = textPrimary,
    inversePrimary = textOnSolidPrimary,
    secondary = textSecondary,
    onSecondary = textOnSolidPrimary,
    secondaryContainer = bgSubtlePrimary,
    onSecondaryContainer = textPrimary,
    tertiary = textSecondary,
    onTertiary = textOnSolidPrimary,
    tertiaryContainer = bgActionPrimaryRest,
    onTertiaryContainer = textOnSolidPrimary,
    background = bgCanvasDefault,
    onBackground = textPrimary,
    surface = bgCanvasDefault,
    onSurface = textPrimary,
    surfaceVariant = bgSubtleSecondary,
    onSurfaceVariant = textSecondary,
    surfaceTint = DarkColorTokens.colorGray1000,
    inverseSurface = DarkColorTokens.colorGray1300,
    inverseOnSurface = textOnSolidPrimary,
    error = textCriticalPrimary,
    onError = textOnSolidPrimary,
    errorContainer = DarkColorTokens.colorRed400,
    onErrorContainer = textCriticalPrimary,
    outline = borderInteractivePrimary,
    outlineVariant = DarkColorTokens.colorAlphaGray400,
    // Note: for light it will be colorGray1400
    scrim = DarkColorTokens.colorGray300,
)
