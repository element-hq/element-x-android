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

open class TimelineItemStickerContentProvider : PreviewParameterProvider<TimelineItemStickerContent> {
    override val values: Sequence<TimelineItemStickerContent>
        get() = sequenceOf(
            aTimelineItemStickerContent(),
            aTimelineItemStickerContent(aspectRatio = 1.0f),
            aTimelineItemStickerContent(aspectRatio = 1.5f),
            aTimelineItemStickerContent(blurhash = null),
        )
}

fun aTimelineItemStickerContent(
    aspectRatio: Float = 0.5f,
    blurhash: String? = A_BLUR_HASH,
) = TimelineItemStickerContent(
    filename = "a sticker.gif",
    caption = "a body",
    formattedCaption = null,
    mediaSource = MediaSource(""),
    thumbnailSource = null,
    mimeType = MimeTypes.IMAGE_JPEG,
    blurhash = blurhash,
    width = null,
    height = 128,
    aspectRatio = aspectRatio,
    formattedFileSize = "4MB",
    fileExtension = "jpg"
)
