/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
fun TimelineItemAudioView(
    content: TimelineItemAudioContent,
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
                imageVector = Icons.Outlined.GraphicEq,
                contentDescription = null,
                tint = ElementTheme.materialColors.primary,
                modifier = Modifier
                    .size(16.dp),
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAudioViewPreview(@PreviewParameter(TimelineItemAudioContentProvider::class) content: TimelineItemAudioContent) =
    ElementPreview {
        TimelineItemAudioView(
            content,
            onContentLayoutChange = {},
        )
    }
