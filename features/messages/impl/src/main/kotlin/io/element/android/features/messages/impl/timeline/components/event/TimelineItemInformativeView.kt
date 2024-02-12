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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemInformativeView(
    text: String,
    iconDescription: String,
    @DrawableRes iconResourceId: Int,
    onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.onSizeChanged { size ->
            onContentLayoutChanged(
                ContentAvoidingLayoutData(
                    contentWidth = size.width,
                    contentHeight = size.height,
                )
            )
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            resourceId = iconResourceId,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = iconDescription,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.secondary,
            style = ElementTheme.typography.fontBodyMdRegular,
            text = text
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemInformativeViewPreview() = ElementPreview {
    TimelineItemInformativeView(
        text = "Info",
        iconDescription = "",
        iconResourceId = CompoundDrawables.ic_compound_delete,
        onContentLayoutChanged = {},
    )
}
