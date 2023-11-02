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

package io.element.android.features.location.api.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.features.location.api.R
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.BooleanProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun StaticMapPlaceholder(
    showProgress: Boolean,
    contentDescription: String?,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    onLoadMapClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = width, height = height)
            .then(if (showProgress) Modifier else Modifier.clickable(onClick = onLoadMapClick))
    ) {
        Image(
            painter = painterResource(id = R.drawable.blurred_map),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width = width, height = height)
        )
        if (showProgress) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Text(text = stringResource(id = CommonStrings.action_static_map_load))
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun StaticMapPlaceholderPreview(
    @PreviewParameter(BooleanProvider::class) values: Boolean
) = ElementPreview {
    StaticMapPlaceholder(
        showProgress = values,
        contentDescription = null,
        width = 400.dp,
        height = 400.dp,
        onLoadMapClick = {},
    )
}
