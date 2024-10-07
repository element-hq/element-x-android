/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody

data class TimelineItemImageContent(
    val body: String,
    val formatted: FormattedBody?,
    val filename: String?,
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
    override val type: String = "TimelineItemImageContent"

    val showCaption = filename != null && filename != body
    val caption = if (showCaption) body else ""

    val preferredMediaSource = if (mimeType == MimeTypes.Gif) {
        mediaSource
    } else {
        thumbnailSource ?: mediaSource
    }
}
