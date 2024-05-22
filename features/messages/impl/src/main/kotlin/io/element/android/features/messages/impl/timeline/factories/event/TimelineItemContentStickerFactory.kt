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
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import javax.inject.Inject

class TimelineItemContentStickerFactory @Inject constructor(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor
) {
    private fun aspectRatioOf(width: Long?, height: Long?): Float? {
        val result = if (height != null && width != null) {
            width.toFloat() / height.toFloat()
        } else {
            null
        }

        return result?.takeIf { it.isFinite() }
    }

    fun create(content: StickerContent): TimelineItemEventContent {
        val aspectRatio = aspectRatioOf(content.info.width, content.info.height)

        return TimelineItemStickerContent(
            body = content.body,
            mediaSource = content.source,
            thumbnailSource = content.info.thumbnailSource,
            mimeType = content.info.mimetype ?: MimeTypes.OctetStream,
            blurhash = content.info.blurhash,
            width = content.info.width?.toInt(),
            height = content.info.height?.toInt(),
            aspectRatio = aspectRatio,
            formattedFileSize = fileSizeFormatter.format(content.info.size ?: 0),
            fileExtension = fileExtensionExtractor.extractFromName(content.body)
        )
    }
}
