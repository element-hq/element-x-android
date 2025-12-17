/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import kotlin.time.Duration.Companion.milliseconds

open class TimelineItemVideoContentProvider : PreviewParameterProvider<TimelineItemVideoContent> {
    override val values: Sequence<TimelineItemVideoContent>
        get() = sequenceOf(
            aTimelineItemVideoContent(),
            aTimelineItemVideoContent(aspectRatio = 1.0f),
            aTimelineItemVideoContent(aspectRatio = 1.5f),
            aTimelineItemVideoContent(blurhash = null),
        )
}

fun aTimelineItemVideoContent(
    aspectRatio: Float = 0.5f,
    blurhash: String? = A_BLUR_HASH,
) = TimelineItemVideoContent(
    filename = "Video.mp4",
    fileSize = 14 * 1024 * 1024L,
    caption = null,
    formattedCaption = null,
    isEdited = false,
    thumbnailSource = null,
    blurHash = blurhash,
    aspectRatio = aspectRatio,
    duration = 100.milliseconds,
    mediaSource = MediaSource(""),
    width = 150,
    height = 300,
    thumbnailWidth = 150,
    thumbnailHeight = 300,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "14MB",
    fileExtension = "mp4"
)
