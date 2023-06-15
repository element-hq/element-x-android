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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun ConnectivityIndicatorView(
    isOnline: Boolean,
    modifier: Modifier = Modifier
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

@Composable
private fun Indicator(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .statusBarsPadding()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        val tint = MaterialTheme.colorScheme.primary
        Image(
            imageVector = Icons.Outlined.WifiOff,
            contentDescription = null,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(StringR.string.common_offline),
            style = ElementTextStyles.Regular.bodyMD.copy(fontWeight = FontWeight.Medium),
            color = tint,
        )
    }
}

@Composable
private fun StatusBarPaddingSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.statusBarsPadding())
}

@Preview
@Composable
internal fun PreviewLightConnectivityIndicatorView() {
    ElementPreviewLight {
        ConnectivityIndicatorView(isOnline = false)
    }
}

@Preview
@Composable
internal fun PreviewDarkConnectivityIndicatorView() {
    ElementPreviewDark {
        ConnectivityIndicatorView(isOnline = false)
    }
}
