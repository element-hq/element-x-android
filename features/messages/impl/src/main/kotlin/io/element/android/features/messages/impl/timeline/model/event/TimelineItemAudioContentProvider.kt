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
import kotlin.time.Duration.Companion.milliseconds

open class TimelineItemAudioContentProvider : PreviewParameterProvider<TimelineItemAudioContent> {
    override val values: Sequence<TimelineItemAudioContent>
        get() = sequenceOf(
            aTimelineItemAudioContent("A sound.mp3"),
            aTimelineItemAudioContent("A bigger name sound.mp3"),
            aTimelineItemAudioContent("An even bigger bigger bigger bigger bigger bigger bigger sound name which doesn't fit .mp3"),
        )
}

fun aTimelineItemAudioContent(fileName: String = "A sound.mp3") = TimelineItemAudioContent(
    filename = fileName,
    caption = null,
    formattedCaption = null,
    mimeType = MimeTypes.Mp3,
    formattedFileSize = "100kB",
    fileExtension = "mp3",
    duration = 100.milliseconds,
    mediaSource = MediaSource(""),
)
