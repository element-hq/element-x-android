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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.toSecondaryEnabledColor

@Composable
fun PreferenceIcon(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    tintColor: Color? = null,
    enabled: Boolean = true,
    isVisible: Boolean = true,
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = tintColor ?: enabled.toSecondaryEnabledColor(),
            modifier = modifier
                .padding(start = 8.dp)
                .width(48.dp)
                .heightIn(max = 48.dp),
        )
    } else if (isVisible) {
        Spacer(modifier = modifier.width(56.dp))
    } else {
        Spacer(modifier = modifier.width(16.dp))
    }
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceIconPreview(@PreviewParameter(ImageVectorProvider::class) content: ImageVector?) =
    ElementThemedPreview { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: ImageVector?) {
    PreferenceIcon(content)
}
