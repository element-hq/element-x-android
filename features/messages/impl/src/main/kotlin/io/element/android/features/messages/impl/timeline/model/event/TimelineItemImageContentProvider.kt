/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.media3.common.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH

open class TimelineItemImageContentProvider : PreviewParameterProvider<TimelineItemImageContent> {
    override val values: Sequence<TimelineItemImageContent>
        get() = sequenceOf(
            aTimelineItemImageContent(),
            aTimelineItemImageContent(aspectRatio = 1.0f),
            aTimelineItemImageContent(aspectRatio = 1.5f),
            aTimelineItemImageContent(blurhash = null),
        )
}

fun aTimelineItemImageContent(
    aspectRatio: Float? = 0.5f,
    blurhash: String? = A_BLUR_HASH,
    filename: String = "A picture.jpg",
    caption: String? = null,
) = TimelineItemImageContent(
    filename = filename,
    fileSize = 4 * 1024 * 1024L,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    mediaSource = MediaSource(""),
    thumbnailSource = null,
    mimeType = MimeTypes.IMAGE_JPEG,
    blurhash = blurhash,
    width = null,
    height = 300,
    thumbnailWidth = null,
    thumbnailHeight = 150,
    aspectRatio = aspectRatio,
    formattedFileSize = "4MB",
    fileExtension = "jpg"
)
