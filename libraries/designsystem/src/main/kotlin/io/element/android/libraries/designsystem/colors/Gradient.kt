/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme

@Composable
@ReadOnlyComposable
fun gradientActionColors(): List<Color> = listOf(
    ElementTheme.colors.gradientActionStop1,
    ElementTheme.colors.gradientActionStop2,
    ElementTheme.colors.gradientActionStop3,
    ElementTheme.colors.gradientActionStop4,
)

@Composable
@ReadOnlyComposable
fun gradientSubtleColors(): List<Color> = listOf(
    ElementTheme.colors.gradientSubtleStop1,
    ElementTheme.colors.gradientSubtleStop2,
    ElementTheme.colors.gradientSubtleStop3,
    ElementTheme.colors.gradientSubtleStop4,
    ElementTheme.colors.gradientSubtleStop5,
    ElementTheme.colors.gradientSubtleStop6,
)

@Composable
@ReadOnlyComposable
fun gradientInfoColors(): List<Color> = listOf(
    ElementTheme.colors.gradientInfoStop1,
    ElementTheme.colors.gradientInfoStop2,
    ElementTheme.colors.gradientInfoStop3,
    ElementTheme.colors.gradientInfoStop4,
    ElementTheme.colors.gradientInfoStop5,
    ElementTheme.colors.gradientInfoStop6,
)
