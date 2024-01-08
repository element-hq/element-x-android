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
import androidx.media3.common.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH

open class TimelineItemStickerContentProvider : PreviewParameterProvider<TimelineItemStickerContent> {
    override val values: Sequence<TimelineItemStickerContent>
        get() = sequenceOf(
            aTimelineItemStickerContent(),
            aTimelineItemStickerContent().copy(aspectRatio = 1.0f),
            aTimelineItemStickerContent().copy(aspectRatio = 1.5f),
        )
}

fun aTimelineItemStickerContent() = TimelineItemStickerContent(
    body = "a body",
    mediaSource = MediaSource(""),
    thumbnailSource = null,
    mimeType = MimeTypes.IMAGE_JPEG,
    blurhash = A_BLUR_HASH,
    width = null,
    height = 128,
    aspectRatio = 0.5f,
    formattedFileSize = "4MB",
    fileExtension = "jpg"
)
