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
import kotlin.time.Duration.Companion.milliseconds

open class TimelineItemAudioContentProvider : PreviewParameterProvider<TimelineItemAudioContent> {
    override val values: Sequence<TimelineItemAudioContent>
        get() = sequenceOf(
            aTimelineItemAudioContent("A sound.mp3"),
            aTimelineItemAudioContent("A bigger name sound.mp3"),
            aTimelineItemAudioContent("An even bigger bigger bigger bigger bigger bigger bigger sound name which doesn't fit.mp3"),
            aTimelineItemAudioContent(caption = "A caption"),
            aTimelineItemAudioContent(caption = "An even bigger bigger bigger bigger bigger bigger bigger caption"),
        )
}

fun aTimelineItemAudioContent(
    fileName: String = "A sound.mp3",
    caption: String? = null,
) = TimelineItemAudioContent(
    filename = fileName,
    fileSize = 100 * 1024L,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    mimeType = MimeTypes.Mp3,
    formattedFileSize = "100kB",
    fileExtension = "mp3",
    duration = 100.milliseconds,
    mediaSource = MediaSource(""),
)
