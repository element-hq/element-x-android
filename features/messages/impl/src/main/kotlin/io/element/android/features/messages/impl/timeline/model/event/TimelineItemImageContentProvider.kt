/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            aTimelineItemImageContent().copy(aspectRatio = 1.0f),
            aTimelineItemImageContent().copy(aspectRatio = 1.5f),
        )
}

fun aTimelineItemImageContent() = TimelineItemImageContent(
    body = "a body",
    formatted = null,
    filename = null,
    mediaSource = MediaSource(""),
    thumbnailSource = null,
    mimeType = MimeTypes.IMAGE_JPEG,
    blurhash = A_BLUR_HASH,
    width = null,
    height = 300,
    aspectRatio = 0.5f,
    formattedFileSize = "4MB",
    fileExtension = "jpg"
)
