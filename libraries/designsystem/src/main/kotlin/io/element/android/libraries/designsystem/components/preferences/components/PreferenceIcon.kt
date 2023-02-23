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

package io.element.android.libraries.designsystem.components.preferences.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.toEnabledColor

@Composable
fun PreferenceIcon(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = enabled.toEnabledColor(),
            modifier = modifier
                .padding(start = 8.dp)
                .width(48.dp),
        )
    } else {
        Spacer(modifier = modifier.width(56.dp))
    }
}

@Preview
@Composable
internal fun PreferenceIconLightPreview(@PreviewParameter(ImageVectorProvider::class) content: ImageVector?) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun PreferenceIconDarkPreview(@PreviewParameter(ImageVectorProvider::class) content: ImageVector?) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: ImageVector?) {
    PreferenceIcon(content)
}
