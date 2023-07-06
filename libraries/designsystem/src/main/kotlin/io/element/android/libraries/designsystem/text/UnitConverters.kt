/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
