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
import androidx.compose.animation.expandVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
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
    // Display the network indicator with an animation
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .background(LocalColors.current.gray400)
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
            Text(text = stringResource(StringR.string.common_offline), style = ElementTextStyles.Regular.bodyMD, color = tint)
        }
    }

    // Show missing status bar padding when the indicator is not visible
    AnimatedVisibility(
        visible = isOnline,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
    }
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
