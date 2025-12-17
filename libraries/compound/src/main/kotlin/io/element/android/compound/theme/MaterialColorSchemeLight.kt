/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.LightColorTokens

/**
 * See the mapping in
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=311-14&p=f&t=QcVyNaPEZMDA6RFK-0
 */
@OptIn(CoreColorToken::class)
fun SemanticColors.toMaterialColorSchemeLight(): ColorScheme = lightColorScheme(
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
    surfaceTint = LightColorTokens.colorGray1000,
    inverseSurface = LightColorTokens.colorGray1300,
    inverseOnSurface = textOnSolidPrimary,
    error = textCriticalPrimary,
    onError = textOnSolidPrimary,
    errorContainer = LightColorTokens.colorRed400,
    onErrorContainer = textCriticalPrimary,
    outline = borderInteractivePrimary,
    outlineVariant = LightColorTokens.colorAlphaGray400,
    // Note: for dark it will be colorGray300
    scrim = LightColorTokens.colorGray1400,
)
