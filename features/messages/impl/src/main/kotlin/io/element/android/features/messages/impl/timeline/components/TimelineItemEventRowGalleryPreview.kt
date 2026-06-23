/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aGalleryItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.anAttachmentItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemGalleryContent(
                        caption = "My vacation photos",
                        items = listOf(
                            aGalleryItem(),
                            aGalleryItem(),
                            aGalleryItem(),
                            aGalleryItem(),
                        ),
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryTwoItemsPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemGalleryContent(
                        items = listOf(
                            aGalleryItem(),
                            aGalleryItem(),
                        ),
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryThreeItemsPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemGalleryContent(
                        caption = "Three photos",
                        items = listOf(
                            aGalleryItem(),
                            aGalleryItem(),
                            aGalleryItem(),
                        ),
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryManyItemsPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemGalleryContent(
                        caption = "Many photos",
                        items = (1..8).map { aGalleryItem() },
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithGalleryVideoItemsPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemGalleryContent(
                        caption = "Videos",
                        items = listOf(
                            aGalleryItem(
                                isVideo = true,
                                duration = kotlin.time.Duration.parse("PT1M30S")
                            ),
                            aGalleryItem(
                                isVideo = true,
                                duration = kotlin.time.Duration.parse("PT45S")
                            ),
                            aGalleryItem(),
                        ),
                    ),
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithAttachmentsPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemAttachmentsContent(
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
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithAttachmentsImagesPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemAttachmentsContent(
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
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithAttachmentsVideosPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemAttachmentsContent(
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
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithAttachmentsAudioPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach { isMine ->
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = isMine,
                    content = aTimelineItemAttachmentsContent(
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
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}
