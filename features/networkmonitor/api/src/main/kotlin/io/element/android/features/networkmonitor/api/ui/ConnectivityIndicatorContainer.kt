/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.networkmonitor.api.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp

private val INDICATOR_VERTICAL_PADDING = 6.dp

/**
 * A view that displays a connectivity indicator when the device is offline.
 */
@Composable
fun ConnectivityIndicatorContainer(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit = {},
) {
    val isIndicatorVisible = remember { MutableTransitionState(!isOnline) }.apply { targetState = !isOnline }
    Column(modifier = modifier) {
        val statusBarTopPadding = if (LocalInspectionMode.current) {
            // Needed to get valid UI previews
            24.dp
        } else {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + INDICATOR_VERTICAL_PADDING
        }
        val target = if (isIndicatorVisible.targetState) statusBarTopPadding else 0.dp
        val topWindowInset by animateDpAsState(
            targetValue = target,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 1.dp,
            ),
            label = "insets-animation",
        )
        // Display the network indicator with an animation
        AnimatedVisibility(
            visibleState = isIndicatorVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ConnectivityIndicator(verticalPadding = INDICATOR_VERTICAL_PADDING)
        }
        // Consume the window insets to avoid double padding.
        content(
            Modifier.consumeWindowInsets(PaddingValues(top = topWindowInset))
        )
    }
}
