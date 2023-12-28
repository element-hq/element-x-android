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

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource

data class TimelineItemStickerContent(
    val body: String,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val formattedFileSize: String,
    val fileExtension: String,
    val mimeType: String,
    val blurhash: String?,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Float?
) : TimelineItemEventContent {
    override val type: String = "TimelineItemStickerContent"

    val preferredMediaSource = if (mimeType == MimeTypes.Gif) {
        mediaSource
    } else {
        thumbnailSource ?: mediaSource
    }
}
