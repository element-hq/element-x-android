/*
 * Copyright 2025 Feral
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Feral custom overlay for the onboarding screen.
 * Dark gradient background matching the iOS design.
 */
@Composable
fun FeralOnBoardingBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF0D0D12),
                        0.5f to Color(0xFF050508),
                        1f to Color.Black,
                    ),
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
