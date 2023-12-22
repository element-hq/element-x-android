/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
