/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Convert Dp to Sp, regarding current density.
 * Can be used for instance to use Dp unit for text.
 */
@Composable
@ReadOnlyComposable
fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }

/**
 * Convert Sp to Dp, regarding current density.
 * Can be used for instance to use Sp unit for size.
 */
@Composable
@ReadOnlyComposable
fun TextUnit.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * Convert Px value to Dp, regarding current density.
 */
@Composable
@ReadOnlyComposable
fun Int.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * Convert Dp value to pixels, regarding current density.
 */
@Composable
@ReadOnlyComposable
fun Dp.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Convert Dp value to pixels, regarding current density.
 */
@Composable
@ReadOnlyComposable
fun Dp.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }
