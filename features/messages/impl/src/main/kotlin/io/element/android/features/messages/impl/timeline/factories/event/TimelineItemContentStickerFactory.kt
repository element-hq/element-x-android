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

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import javax.inject.Inject

class TimelineItemContentStickerFactory @Inject constructor() {

    fun create(stickerContent: StickerContent): TimelineItemEventContent {
        val mediaSource = MediaSource(url = stickerContent.url)
        // TODO: FIXME: figure tf out what that is needed for
        val formattedFileSize = "tf?"
        val fileExtension = "png"
        val mimeType = MimeTypes.Png
        return TimelineItemImageContent(
            body = stickerContent.body,
            mediaSource = mediaSource,
            thumbnailSource = null,
            formattedFileSize = formattedFileSize,
            fileExtension = fileExtension,
            mimeType = mimeType,
            blurhash = null,
            width = null,
            height = null,
            aspectRatio = null
        )
    }
}
