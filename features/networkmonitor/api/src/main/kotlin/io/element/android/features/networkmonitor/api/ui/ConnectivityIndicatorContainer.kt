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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A view that displays a connectivity indicator when the device is offline, passing the padding
 * needed to make sure the status bar is not overlapped to its content views.
 */
@Composable
fun ConnectivityIndicatorContainer(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (topPadding: Dp) -> Unit = {},
) {
    val isIndicatorVisible = remember { MutableTransitionState(!isOnline) }.apply { targetState = !isOnline }

    val statusBarTopPadding = if (LocalInspectionMode.current) {
        // Needed to get valid UI previews
        24.dp
    } else {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 6.dp
    }
    val target = remember(isIndicatorVisible.targetState, statusBarTopPadding) {
        if (!isIndicatorVisible.targetState) 0.dp else statusBarTopPadding
    }
    val animationStateOffset by animateDpAsState(
        targetValue = target,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 1.dp,
        ),
        label = "insets-animation",
    )

    content(animationStateOffset)

    // Display the network indicator with an animation
    AnimatedVisibility(
        visibleState = isIndicatorVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Indicator(modifier)
    }
}
