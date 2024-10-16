/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            filename = content.filename,
            caption = content.body,
            formattedCaption = null,
            mediaSource = content.source,
            thumbnailSource = content.info.thumbnailSource,
            mimeType = content.info.mimetype ?: MimeTypes.OctetStream,
            blurhash = content.info.blurhash,
            width = content.info.width?.toInt(),
            height = content.info.height?.toInt(),
            aspectRatio = aspectRatio,
            formattedFileSize = fileSizeFormatter.format(content.info.size ?: 0),
            fileExtension = fileExtensionExtractor.extractFromName(content.filename)
        )
    }
}
