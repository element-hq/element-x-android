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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContentProvider
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemFileView(
    content: TimelineItemFileContent,
    onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = 32.dp
    val spacing = 8.dp
    Row(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .background(ElementTheme.materialColors.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                resourceId = CompoundDrawables.ic_compound_attachment,
                contentDescription = null,
                tint = ElementTheme.materialColors.primary,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(-45f),
            )
        }
        Spacer(Modifier.width(spacing))
        Column {
            Text(
                text = content.body,
                color = ElementTheme.materialColors.primary,
                maxLines = 2,
                style = ElementTheme.typography.fontBodyLgRegular,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = content.fileExtensionAndSize,
                color = ElementTheme.materialColors.secondary,
                style = ElementTheme.typography.fontBodySmRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = ContentAvoidingLayout.measureLastTextLine(
                    onContentLayoutChanged = onContentLayoutChanged,
                    extraWidth = iconSize + spacing
                )
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemFileViewPreview(@PreviewParameter(TimelineItemFileContentProvider::class) content: TimelineItemFileContent) = ElementPreview {
    TimelineItemFileView(
        content,
        onContentLayoutChanged = {},
    )
}
