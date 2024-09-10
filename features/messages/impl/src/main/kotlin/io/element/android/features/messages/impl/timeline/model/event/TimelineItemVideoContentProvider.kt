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
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import kotlin.time.Duration.Companion.milliseconds

open class TimelineItemVideoContentProvider : PreviewParameterProvider<TimelineItemVideoContent> {
    override val values: Sequence<TimelineItemVideoContent>
        get() = sequenceOf(
            aTimelineItemVideoContent(),
            aTimelineItemVideoContent().copy(aspectRatio = 1.0f),
            aTimelineItemVideoContent().copy(aspectRatio = 1.5f),
        )
}

fun aTimelineItemVideoContent() = TimelineItemVideoContent(
    body = "Video.mp4",
    formatted = null,
    filename = null,
    thumbnailSource = null,
    blurHash = A_BLUR_HASH,
    aspectRatio = 0.5f,
    duration = 100.milliseconds,
    videoSource = MediaSource(""),
    height = 300,
    width = 150,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "14MB",
    fileExtension = "mp4"
)
