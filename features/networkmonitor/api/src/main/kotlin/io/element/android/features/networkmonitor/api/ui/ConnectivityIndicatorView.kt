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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * A view that displays a connectivity indicator when the device is offline, adding a default
 * padding to make sure the status bar is not overlapped.
 */
@Composable
fun ConnectivityIndicatorView(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    val isIndicatorVisible = remember { MutableTransitionState(!isOnline) }.apply { targetState = !isOnline }
    val isStatusBarPaddingVisible = remember { MutableTransitionState(isOnline) }.apply { targetState = isOnline }

    // Display the network indicator with an animation
    AnimatedVisibility(
        visibleState = isIndicatorVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Indicator(modifier)
    }

    // Show missing status bar padding when the indicator is not visible
    AnimatedVisibility(
        visibleState = isStatusBarPaddingVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        StatusBarPaddingSpacer(modifier)
    }
}

/**
 * A view that displays a connectivity indicator when the device is offline, passing the padding
 * needed to make sure the status bar is not overlapped to its content views.
 */
@Composable
fun ConnectivityIndicatorContainer(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (topPadding: Dp) -> Unit,
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

@Composable
private fun Indicator(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .statusBarsPadding()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = MaterialTheme.colorScheme.primary
        Icon(
            resourceId = CommonDrawables.ic_compound_offline,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.sp.toDp()),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(CommonStrings.common_offline),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = tint,
        )
    }
}

@Composable
private fun StatusBarPaddingSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.statusBarsPadding())
}

@DayNightPreviews
@Composable
internal fun ConnectivityIndicatorViewPreview() {
    ElementPreview {
        ConnectivityIndicatorView(isOnline = false)
    }
}
