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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.features.location.api.R
import io.element.android.libraries.ui.strings.R.string as StringR

@Composable
internal fun StaticMapPlaceholder(
    showProgress: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    darkMode: Boolean = !ElementTheme.colors.isLight,
    onLoadMapClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                id = if (darkMode) R.drawable.blurred_map_dark
                else R.drawable.blurred_map_light
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
        )
        if (showProgress) {
            CircularProgressIndicator()
        } else {
            Box(
                modifier = modifier.clickable(onClick = onLoadMapClick),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Text(text = stringResource(id = StringR.action_static_map_load))
                }
            }
        }
    }
}

@Preview
@Composable
fun StaticMapPlaceholderLightPreview(
    @PreviewParameter(BooleanParameterProvider::class) values: Boolean
) = ElementPreviewLight { ContentToPreview(values) }

@Preview
@Composable
fun StaticMapPlaceholderDarkPreview(
    @PreviewParameter(BooleanParameterProvider::class) values: Boolean
) = ElementPreviewDark { ContentToPreview(values) }

@Composable
private fun ContentToPreview(showProgress: Boolean) {
    StaticMapPlaceholder(
        showProgress = showProgress,
        contentDescription = null,
        modifier = Modifier.size(400.dp),
        onLoadMapClick = {},
    )
}

private class BooleanParameterProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}
