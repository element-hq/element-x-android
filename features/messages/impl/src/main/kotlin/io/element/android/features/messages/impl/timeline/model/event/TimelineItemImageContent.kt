/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAnimatedImage
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_HEIGHT
import io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_WIDTH
import io.element.android.libraries.matrix.ui.media.MediaRequestData

data class TimelineItemImageContent(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val formattedFileSize: String,
    val fileExtension: String,
    val mimeType: String,
    val blurhash: String?,
    val width: Int?,
    val height: Int?,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val aspectRatio: Float?
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemImageContent"

    val showCaption = caption != null

    val thumbnailMediaRequest: MediaRequestData by lazy {
        val kind = when (preferredMediaSource) {
            mediaSource -> MediaRequestData.Kind.File(
                fileName = filename,
                mimeType = mimeType
            )
            else -> MediaRequestData.Kind.Thumbnail(
                width = thumbnailWidth?.toLong() ?: MAX_THUMBNAIL_WIDTH,
                height = thumbnailHeight?.toLong() ?: MAX_THUMBNAIL_HEIGHT
            )
        }
        MediaRequestData(source = preferredMediaSource, kind = kind)
    }

    val preferredMediaSource = if (mimeType.isMimeTypeAnimatedImage()) {
        mediaSource
    } else {
        thumbnailSource ?: mediaSource
    }
}
