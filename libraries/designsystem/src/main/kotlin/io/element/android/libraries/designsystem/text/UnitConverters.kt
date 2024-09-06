/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * Convert Dp to Sp, regarding current density.
 * Can be used for instance to use Dp unit for text.
 */
@Composable
fun Dp.toSp(): TextUnit = with(LocalDensity.current) { toSp() }

/**
 * Convert Sp to Dp, regarding current density.
 * Can be used for instance to use Sp unit for size.
 */
@Composable
fun TextUnit.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * Convert Px value to Dp, regarding current density.
 */
@Composable
fun Int.toDp(): Dp = with(LocalDensity.current) { toDp() }

/**
 * Convert Dp value to pixels, regarding current density.
 */
@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { toPx() }

/**
 * Convert Dp value to pixels, regarding current density.
 */
@Composable
fun Dp.roundToPx(): Int = with(LocalDensity.current) { roundToPx() }
