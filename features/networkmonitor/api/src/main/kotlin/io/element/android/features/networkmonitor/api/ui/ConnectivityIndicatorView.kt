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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * A view that displays a connectivity indicator when the device is offline, adding a default
 * padding to make sure the status bar is not overlapped.
 */
@Composable
fun ConnectivityIndicatorView(
    isOnline: Boolean,
) {
    val isIndicatorVisible = remember { MutableTransitionState(!isOnline) }.apply { targetState = !isOnline }
    val isStatusBarPaddingVisible = remember { MutableTransitionState(isOnline) }.apply { targetState = isOnline }

    // Display the network indicator with an animation
    AnimatedVisibility(
        visibleState = isIndicatorVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Indicator()
    }

    // Show missing status bar padding when the indicator is not visible
    AnimatedVisibility(
        visibleState = isStatusBarPaddingVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        StatusBarPaddingSpacer()
    }
}

@Composable
private fun StatusBarPaddingSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.statusBarsPadding())
}

@PreviewsDayNight
@Composable
internal fun ConnectivityIndicatorViewPreview() {
    ElementPreview {
        ConnectivityIndicatorView(isOnline = false)
    }
}
