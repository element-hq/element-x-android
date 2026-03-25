/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun alphaAnimation(
    fromAlpha: Float = 0f,
    toAlpha: Float = 1f,
    label: String = "AlphaAnimation",
): State<Float> {
    if (isReduceMotionEnabled) {
        return remember { mutableFloatStateOf(toAlpha) }
    }
    val firstAlpha = if (LocalInspectionMode.current) 1f else fromAlpha
    var alpha by remember { mutableFloatStateOf(firstAlpha) }
    LaunchedEffect(Unit) { alpha = toAlpha }
    return animateFloatAsState(
        targetValue = alpha,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = label,
    )
}
