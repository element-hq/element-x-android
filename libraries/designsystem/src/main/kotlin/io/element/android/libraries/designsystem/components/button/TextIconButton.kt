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

package io.element.android.libraries.designsystem.components.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton

@Composable
fun TextIconButton(
    imageVector: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    onClick: () -> Unit = {},
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Image(
            imageVector = imageVector,
            contentDescription = "",
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = text,
        )
    }
}

@Preview
@Composable
internal fun TextIconButtonPreviewLight() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun TextIconButtonPreviewDark() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    Column {
        TextIconButton(
            modifier = Modifier.padding(horizontal = 8.dp),
            imageVector = ImageVector.vectorResource(R.drawable.ic_groups),
            text = "Click me!",
            onClick = {},
        )
    }
}
