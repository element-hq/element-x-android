/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.components.event.aGalleryItem
import io.element.android.features.messages.impl.timeline.components.event.aTimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.GalleryItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.anAttachmentItem
import kotlin.time.Duration

class TimelineItemEventContentForGalleryViewProvider :
    PreviewParameterProvider<TimelineItemEventContent> {
    override val values: Sequence<TimelineItemEventContent>
        get() = sequenceOf(
            aTimelineItemGalleryContent(
                caption = "My vacation photos",
                items = listOf(
                    aGalleryItem(),
                    aGalleryItem(),
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                caption = "Three photos",
                items = listOf(
                    aGalleryItem(),
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                caption = "Many photos",
                items = (1..8).map { aGalleryItem() },
            ),
            aTimelineItemGalleryContent(
                caption = "Videos",
                items = listOf(
                    aGalleryItem(
                        type = GalleryItem.Type.Video,
                        duration = Duration.parse("PT1M30S")
                    ),
                    aGalleryItem(
                        type = GalleryItem.Type.Video,
                        duration = Duration.parse("PT45S")
                    ),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemAttachmentsContent(
                caption = "Documents",
                attachments = listOf(
                    anAttachmentItem(
                        filename = "document.pdf",
                        fileExtension = "pdf",
                    ),
                    anAttachmentItem(
                        filename = "presentation.pdf",
                        fileExtension = "pdf",
                    ),
                    anAttachmentItem(
                        filename = "spreadsheet.xlsx",
                        fileExtension = "xlsx",
                    ),
                ),
            ),
            aTimelineItemAttachmentsContent(
                caption = "Photos",
                attachments = listOf(
                    anAttachmentItem(
                        filename = "photo1.jpg",
                        fileExtension = "jpg",
                        hasThumbnail = true,
                    ),
                    anAttachmentItem(
                        filename = "photo2.jpg",
                        fileExtension = "jpg",
                        hasThumbnail = true,
                    ),
                    anAttachmentItem(
                        filename = "photo3.jpg",
                        fileExtension = "jpg",
                        hasThumbnail = true,
                    ),
                ),
            ),
            aTimelineItemAttachmentsContent(
                caption = "Videos",
                attachments = listOf(
                    anAttachmentItem(
                        filename = "video1.mp4",
                        fileExtension = "mp4",
                        hasThumbnail = true,
                        fileSize = 150_000_000L,
                        formattedFileSize = "150MB",
                    ),
                    anAttachmentItem(
                        filename = "video2.mov",
                        fileExtension = "mov",
                        hasThumbnail = true,
                        fileSize = 85_000_000L,
                        formattedFileSize = "85MB",
                    ),
                ),
            ),
            aTimelineItemAttachmentsContent(
                caption = "Audio",
                attachments = listOf(
                    anAttachmentItem(
                        filename = "recording.mp3",
                        fileExtension = "mp3",
                        fileSize = 4_500_000L,
                        formattedFileSize = "4.5MB",
                    ),
                    anAttachmentItem(
                        filename = "voice_message.m4a",
                        fileExtension = "m4a",
                        fileSize = 1_200_000L,
                        formattedFileSize = "1.2MB",
                    ),
                ),
            ),
        )
}
