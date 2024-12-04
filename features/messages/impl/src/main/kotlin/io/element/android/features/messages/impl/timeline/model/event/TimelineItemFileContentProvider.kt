/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource

open class TimelineItemFileContentProvider : PreviewParameterProvider<TimelineItemFileContent> {
    override val values: Sequence<TimelineItemFileContent>
        get() = sequenceOf(
            aTimelineItemFileContent(),
            aTimelineItemFileContent("A bigger name file.pdf"),
            aTimelineItemFileContent("An even bigger bigger bigger bigger bigger bigger bigger file name which doesn't fit.pdf"),
            aTimelineItemFileContent(caption = "A caption"),
            aTimelineItemFileContent(caption = "An even bigger bigger bigger bigger bigger bigger bigger caption"),
        )
}

fun aTimelineItemFileContent(
    fileName: String = "A file.pdf",
    caption: String? = null,
) = TimelineItemFileContent(
    filename = fileName,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    thumbnailSource = null,
    mediaSource = MediaSource(url = ""),
    mimeType = MimeTypes.Pdf,
    formattedFileSize = "100kB",
    fileExtension = "pdf"
)
