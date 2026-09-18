/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class TimelineItemCircleContentPreviewParam : PreviewParameterProvider<TimelineItemCircleContent> {
    override val values: Sequence<TimelineItemCircleContent>
        get() = sequenceOf(
            aTimelineItemCircleContent(),
            aTimelineItemCircleContent(blurHash = null),
        )
}

fun aTimelineItemCircleContent(
    eventId: EventId? = EventId("\$anEventId"),
    duration: Duration = 8.seconds,
    blurHash: String? = A_BLUR_HASH,
    mediaSource: MediaSource = MediaSource(""),
    thumbnailSource: MediaSource? = MediaSource(""),
) = TimelineItemCircleContent(
    eventId = eventId,
    filename = "circle.mp4",
    fileSize = 1024 * 512L,
    caption = null,
    formattedCaption = null,
    isEdited = false,
    duration = duration,
    mediaSource = mediaSource,
    thumbnailSource = thumbnailSource,
    blurHash = blurHash,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "512 KB",
    fileExtension = "mp4",
)
