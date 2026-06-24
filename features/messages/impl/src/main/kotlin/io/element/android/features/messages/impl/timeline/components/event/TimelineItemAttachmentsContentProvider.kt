/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.anAttachmentItem
import io.element.android.libraries.matrix.api.media.MediaSource

class TimelineItemAttachmentsContentProvider : PreviewParameterProvider<TimelineItemAttachmentsContent> {
    override val values: Sequence<TimelineItemAttachmentsContent>
        get() = sequenceOf(
            aTimelineItemAttachmentsContent(
                body = "Files",
                caption = null,
                attachments = listOf(
                    anAttachmentItem(
                        filename = "document.pdf",
                        mimeType = "application/pdf",
                        fileSize = null,
                        formattedFileSize = "2.5 MB",
                        fileExtension = "PDF",
                    ),
                    anAttachmentItem(
                        filename = "photo.jpg",
                        mimeType = "image/jpeg",
                        thumbnailSource = MediaSource(url = "thumb", json = ""),
                        fileSize = null,
                        formattedFileSize = "1.2 MB",
                        fileExtension = "JPG",
                    ),
                    anAttachmentItem(
                        filename = "spreadsheet.xlsx",
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        fileSize = null,
                        formattedFileSize = "450 KB",
                        fileExtension = "XLSX",
                    ),
                ),
            ),
            aTimelineItemAttachmentsContent(
                body = "Files",
                caption = "Important documents",
                attachments = listOf(
                    anAttachmentItem(
                        filename = "report.pdf",
                        mimeType = "application/pdf",
                        fileSize = null,
                        formattedFileSize = "3.2 MB",
                        fileExtension = "PDF",
                    ),
                    anAttachmentItem(
                        filename = "notes.txt",
                        mimeType = "text/plain",
                        fileSize = null,
                        formattedFileSize = "12 KB",
                        fileExtension = "TXT",
                    ),
                ),
            ),
        )
}
