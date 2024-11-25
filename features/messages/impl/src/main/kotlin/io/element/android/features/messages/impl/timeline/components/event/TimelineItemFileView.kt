/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContentProvider
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun TimelineItemFileView(
    content: TimelineItemFileContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    TimelineItemAttachmentView(
        filename = content.filename,
        fileExtensionAndSize = content.fileExtensionAndSize,
        caption = content.caption,
        onContentLayoutChange = onContentLayoutChange,
        modifier = modifier,
        icon = {
            Icon(
                resourceId = CompoundDrawables.ic_compound_attachment,
                contentDescription = null,
                tint = ElementTheme.materialColors.primary,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(-45f),
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemFileViewPreview(@PreviewParameter(TimelineItemFileContentProvider::class) content: TimelineItemFileContent) = ElementPreview {
    TimelineItemFileView(
        content,
        onContentLayoutChange = {},
    )
}
