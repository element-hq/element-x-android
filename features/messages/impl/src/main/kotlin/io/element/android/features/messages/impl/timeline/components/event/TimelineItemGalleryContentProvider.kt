/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.model.event.GalleryItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimelineItemGalleryContentProvider : PreviewParameterProvider<TimelineItemGalleryContent> {
    override val values: Sequence<TimelineItemGalleryContent>
        get() = sequenceOf(
            aTimelineItemGalleryContent(
                caption = "My vacation photos",
                items = listOf(
                    aGalleryItem(),
                    aGalleryItem(type = GalleryItem.Type.Video, duration = 65.seconds),
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(width = 1920, height = 1080),
                    aGalleryItem(width = 1600, height = 900),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(width = 1080, height = 1920),
                    aGalleryItem(width = 900, height = 1600),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(width = 1920, height = 1080),
                    aGalleryItem(width = 1080, height = 1920),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(type = GalleryItem.Type.Video, duration = 45.seconds),
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                caption = "2 landscape + 1 portrait",
                items = listOf(
                    aGalleryItem(width = 1920, height = 1080),
                    aGalleryItem(width = 1600, height = 900),
                    aGalleryItem(width = 1080, height = 1920),
                ),
            ),
            aTimelineItemGalleryContent(
                caption = "1 landscape + 2 portrait",
                items = listOf(
                    aGalleryItem(width = 1920, height = 1080),
                    aGalleryItem(width = 1080, height = 1920),
                    aGalleryItem(width = 900, height = 1600),
                ),
            ),
            aTimelineItemGalleryContent(
                items = listOf(
                    aGalleryItem(),
                    aGalleryItem(),
                    aGalleryItem(type = GalleryItem.Type.Video, duration = 120.seconds),
                    aGalleryItem(),
                    aGalleryItem(),
                ),
            ),
            aTimelineItemGalleryContent(
                caption = "Many photos",
                items = (1..12).map {
                    aGalleryItem(
                        type = if (it == 3) GalleryItem.Type.Video else GalleryItem.Type.Image,
                    )
                },
            ),
        )
}

fun aTimelineItemGalleryContent(
    body: String = "Gallery",
    caption: String? = null,
    items: List<GalleryItem> = listOf(
        aGalleryItem(),
        aGalleryItem(),
        aGalleryItem(),
        aGalleryItem(),
    ),
) = TimelineItemGalleryContent(
    body = body,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    items = items.toImmutableList(),
)

fun aGalleryItem(
    filename: String = "photo.jpg",
    type: GalleryItem.Type = GalleryItem.Type.Image,
    width: Int = 400,
    height: Int = 300,
    duration: Duration = Duration.ZERO,
): GalleryItem {
    return GalleryItem(
        filename = filename,
        mimeType = when (type) {
            GalleryItem.Type.Video -> "video/mp4"
            GalleryItem.Type.Audio -> "audio/mpeg"
            GalleryItem.Type.File -> "application/pdf"
            GalleryItem.Type.Image -> "image/jpeg"
        },
        mediaSource = MediaSource(url = "", json = ""),
        thumbnailSource = null,
        width = width,
        height = height,
        thumbnailWidth = width,
        thumbnailHeight = height,
        blurhash = null,
        type = type,
        duration = duration,
    )
}
